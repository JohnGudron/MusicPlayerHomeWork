package com.example.mymusicplayer

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class MyPlayingService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        println("service created")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        println("service started")
        Executors.newSingleThreadExecutor().execute {
            if (intent.action == "pause") {
                if (mediaPlayer != null) mediaPlayer!!.pause()
            }
            else {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        setDataSource(
                            applicationContext,
                            intent.getParcelableExtra<Uri?>("song")!!
                        )
                        prepareAsync()
                        setOnPreparedListener { it.start() }
                        setOnCompletionListener {
                            it.release()
                            stopSelf()
                        }
                    }
                } else {
                    when (intent.action) {
                        "play" -> mediaPlayer!!.start()
                        "other" -> {
                            mediaPlayer!!.apply {
                                reset()
                                setDataSource(
                                    applicationContext,
                                    intent.getParcelableExtra<Uri?>("song")!!
                                )
                                prepareAsync()
                            }
                        }
                        else -> throw Exception("something wrong in the started Intent")
                    }

                }

            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        println("service destroyed")
    }
}