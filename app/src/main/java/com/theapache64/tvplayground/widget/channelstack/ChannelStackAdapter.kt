package com.theapache64.tvplayground.widget.channelstack

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.theapache64.tvplayground.databinding.ItemChannelStackBinding
import com.theapache64.tvplayground.utils.GlideApp
import com.theapache64.tvplayground.utils.GlideRequest

/**
 * Created by theapache64 : Nov 20 Fri,2020 @ 19:31
 */
class ChannelStackAdapter(
    private val context: Context,
    private val preloadSizeProvider: ViewPreloadSizeProvider<Channel>,
    val channels: MutableList<Channel>
) : RecyclerView.Adapter<ChannelStackAdapter.ViewHolder>(),
    ListPreloader.PreloadModelProvider<Channel> {

    companion object {
        private const val THUMB_SIZE = 75
    }

    private val layoutInflater by lazy { LayoutInflater.from(context) }

    // Glide things
    private var fullRequest: GlideRequest<Drawable>
    private var thumbRequest: GlideRequest<Drawable>
    val glideRequests = GlideApp.with(context)

    init {
        // Creating glide request managers
        fullRequest = glideRequests
            .asDrawable()

        thumbRequest = glideRequests.asDrawable()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .override(THUMB_SIZE)
            .priority(Priority.HIGH)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChannelStackBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        ).apply {
            // Informing glide pre-loader about the viewport size
            preloadSizeProvider.setView(binding.ivChannelLogo)
        }
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

    inner class ViewHolder(val binding: ItemChannelStackBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: Channel) {
            binding.channel = channel

            // Focused channel UI state
            if (channel.isActive) {
                binding.root.setBackgroundColor(Color.RED)
            }

            // Playing channel focus UI state
            if (channel.isPlaying) {
                binding.root.setBackgroundColor(Color.BLUE)
            }

            // Default channel state
            if (channel.isPlaying.not() && channel.isActive.not()) {
                binding.root.setBackgroundColor(Color.TRANSPARENT)
            }

            // Load image
            fullRequest.load(channel.imageUrl)
                .thumbnail(thumbRequest.load(channel.imageUrl))
                .into(binding.ivChannelLogo)
        }
    }

    override fun getItemId(position: Int): Long {
        return RecyclerView.NO_ID
    }

    override fun getPreloadItems(viewPosition: Int): MutableList<Channel> {
        val listPosition = getListPositionFrom(viewPosition)
        return mutableListOf(channels[listPosition])
    }

    override fun getPreloadRequestBuilder(item: Channel): RequestBuilder<*>? {
        return fullRequest
            .thumbnail(thumbRequest.load(item.imageUrl))
            .load(item.imageUrl)
    }
}