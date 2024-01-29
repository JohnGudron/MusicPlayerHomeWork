package com.example.mymusicplayer

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: SongRecyclerAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var songList: List<Song>
    private var currentInd: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        songList = findSongs()

        val playBtn = findViewById<ImageButton>(R.id.activity_play_Btn)
        val pauseBtn = findViewById<ImageButton>(R.id.activity_pause_btn)
        val nextBTN = findViewById<ImageButton>(R.id.activity_next_Btn)
        val prevBtn = findViewById<ImageButton>(R.id.activity_prev_Btn)
        val searchBtn = findViewById<Button>(R.id.activity_search_btn)

        searchBtn.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this.applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    songList = findSongs()
                    adapter.data = songList.toMutableList()
                    adapter.notifyDataSetChanged()
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) -> {
                    AlertDialog.Builder(this)
                        .setTitle("Permission required")
                        .setMessage("This app needs permission to access this feature.")
                        .setPositiveButton("Grant") { _, _ ->
                            ActivityCompat.requestPermissions(
                                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                1,
                            )
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1,
                    )
                }
            }
        }

        playBtn.setOnClickListener {
            val intent = Intent(this, MyPlayingService::class.java).apply {
                action = "play"
                putExtra("song", songList[currentInd].uri)
            }
            startService(intent)
        }
        pauseBtn.setOnClickListener {
            val intent = Intent(this, MyPlayingService::class.java).apply {
                action = "pause"
                putExtra("song", songList[currentInd].uri)
            }
            startService(intent)
        }
        nextBTN.setOnClickListener {
            if (currentInd == songList.lastIndex) currentInd = 0
            else currentInd++

            val intent = Intent(this, MyPlayingService::class.java).apply {
                action = "other"
                putExtra("song", songList[currentInd].uri)
            }
            startService(intent)
        }
        prevBtn.setOnClickListener {
            if (currentInd == 0) currentInd = songList.lastIndex
            else currentInd--
            val intent = Intent(this, MyPlayingService::class.java).apply {
                action = "other"
                putExtra("song", songList[currentInd].uri)
            }
            startService(intent)
        }

        recycler = findViewById(R.id.songRecycler)
        adapter = SongRecyclerAdapter(songList, ::onItemPlayBtnClick, ::onItemPauseBtnClick)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)



    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    songList = findSongs()
                    adapter.data = songList.toMutableList()
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "Songs cannot be loaded without permission", Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }


    private fun findSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val uri =
            if (Build.VERSION.SDK_INT >= 29) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        val selection = ""
        val selectionArgs = arrayOf<String>()
        val sortOrder = ""
        val query = applicationContext.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                // Then, we get the values of columns for a given image.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val artist = cursor.getString(artistColumn)
                val duration = cursor.getLong(durationColumn)

                // Finally, we store the result in our defined list.
                if (artist != null) songs.add(Song(id, name, artist, duration, ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id)))
            }
        }

        return songs.ifEmpty {
            listOf(
                Song(
                    1L,
                    "about_services_first",
                    "bot",
                    74000L,
                    Uri.parse("android.resource://${packageName}/${R.raw.about_services_first}")
                ),
                Song(
                    1L,
                    "about_services_second",
                    "bot",
                    65000L,
                    Uri.parse("android.resource://${packageName}/${R.raw.about_services_second}")
                )
            )
        }
    }

    private fun onItemPlayBtnClick(song: Song) {
        val oldInd = currentInd
        currentInd = songList.indexOf(song)

        val intent = Intent(this, MyPlayingService::class.java).apply {
            action = if (oldInd == currentInd) "play" else "other"
            putExtra("song", song.uri)
        }
        startService(intent)
    }
    private fun onItemPauseBtnClick(song: Song) {
        val oldInd = currentInd
        currentInd = songList.indexOf(song)
        if (oldInd == currentInd) {
            val intent = Intent(this, MyPlayingService::class.java).apply {
                action = "pause"
                putExtra("song", songList[currentInd].uri)
            }
            startService(intent)
        }
    }
}

