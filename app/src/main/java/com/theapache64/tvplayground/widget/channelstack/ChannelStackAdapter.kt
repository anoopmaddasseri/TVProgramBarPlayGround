package com.theapache64.tvplayground.widget.channelstack

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theapache64.tvplayground.databinding.ItemChannelStackBinding
import timber.log.Timber

/**
 * Created by theapache64 : Nov 20 Fri,2020 @ 19:31
 */
class ChannelStackAdapter(
    val channels: MutableList<Channel>
) : RecyclerView.Adapter<ChannelStackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChannelStackBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, viewPosition: Int) {
        val listPosition = getListPositionFrom(viewPosition)
        val channel = channels[listPosition]
        holder.bind(channel)
    }

    fun getListPositionFrom(viewPosition: Int): Int {
        return viewPosition % channels.size
    }

    override fun getItemCount(): Int = Int.MAX_VALUE


    inner class ViewHolder(private val binding: ItemChannelStackBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: Channel) {
            binding.channel = channel

            if (channel.isActive) {
                binding.root.setBackgroundColor(Color.RED)
            } else {
                binding.root.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }
}