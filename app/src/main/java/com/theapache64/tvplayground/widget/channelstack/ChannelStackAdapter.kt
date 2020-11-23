package com.theapache64.tvplayground.widget.channelstack

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.theapache64.tvplayground.databinding.ItemChannelStackBinding

/**
 * Created by theapache64 : Nov 20 Fri,2020 @ 19:31
 */
class ChannelStackAdapter(
    private val context: Context,
    val channels: MutableList<Channel>
) : RecyclerView.Adapter<ChannelStackAdapter.ViewHolder>(),
    ListPreloader.PreloadModelProvider<Channel> {

    private val layoutInflater by lazy { LayoutInflater.from(context) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChannelStackBinding.inflate(
                layoutInflater,
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

    override fun getPreloadItems(viewPosition: Int): MutableList<Channel> {
        val listPosition = getListPositionFrom(viewPosition)
        return mutableListOf(channels[listPosition])
    }

    override fun getPreloadRequestBuilder(item: Channel): RequestBuilder<*>? {
        return Glide.with(context)
            .load(item.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
    }
}