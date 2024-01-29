package com.example.mymusicplayer

import android.net.Uri

data class Song(val id: Long, val name: String, val artist: String, val duration: Long, val uri:Uri)
