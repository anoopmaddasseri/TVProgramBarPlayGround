package com.theapache64.tvplayground.widget.programbar

/**
 * Created by Anoop Maddasseri : Nov 29 Sun,2020 @ 09:17
 */
data class Program(
    val id: String,
    val title: Int,
    val imageUrl: String,
    var isActive: Boolean,
    var isPlaying: Boolean,
    var startAt: Long? = null,
    var endAt: Long? = null
)