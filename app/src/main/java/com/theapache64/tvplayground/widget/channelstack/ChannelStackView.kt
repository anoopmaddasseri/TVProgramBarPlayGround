package com.theapache64.tvplayground.widget.channelstack

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by theapache64 : Nov 20 Fri,2020 @ 17:43
 */
class ChannelStackView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var channelStackAdapter: ChannelStackAdapter? = null

    init {
        layoutManager = LinearLayoutManager(context)
    }

    fun setChannels(channels: List<Channel>) {
        channelStackAdapter = ChannelStackAdapter(channels)
        this.adapter = channelStackAdapter
    }

}