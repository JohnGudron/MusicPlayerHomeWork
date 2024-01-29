package com.example.mymusicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongRecyclerAdapter(
    data: List<Song>,
    private val onItemPlayBtnClick: (Song) -> Unit,
    private val onItemPauseBtnClick: (Song) -> Unit
) : RecyclerView.Adapter<SongRecyclerAdapter.SongViewHolder>() {

    var data = data.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val playBtn = view.findViewById<ImageButton>(R.id.item_play_btn)
        private val pauseBtn = view.findViewById<ImageButton>(R.id.item_pause_btn)
        private val authorTv = view.findViewById<TextView>(R.id.author_tv)
        private val nameTv = view.findViewById<TextView>(R.id.name_tv)
        private val durationTv = view.findViewById<TextView>(R.id.duration_tv)

        fun bind(item: Song) {
            authorTv.text = item.artist
            nameTv.text = item.name
            durationTv.text = item.duration.toFormattedTime()
            playBtn.setOnClickListener {
                onItemPlayBtnClick(item)
            }
            pauseBtn.setOnClickListener {
                onItemPauseBtnClick(item)
            }
        }
    }


}
fun Long.toFormattedTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
