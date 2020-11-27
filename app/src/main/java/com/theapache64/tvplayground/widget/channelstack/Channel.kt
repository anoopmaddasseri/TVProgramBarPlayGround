package com.theapache64.tvplayground.widget.channelstack

/**
 * Created by theapache64 : Nov 20 Fri,2020 @ 19:28
 */
data class Channel(
    val id : String,
    val no: Int,
    val imageUrl: String,
    var isActive: Boolean,
    var isPlaying: Boolean
)