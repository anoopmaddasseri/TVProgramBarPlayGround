package com.theapache64.tvplayground.widget.channelstack

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider

/**
 * Created by theapache64 : Nov 20 Fri,2020 @ 17:43
 */
class ChannelStackView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    companion object {
        private const val PRELOAD_COUNT = 50
    }

    private var currentViewPosition: Int = -1
    private var prevViewPosition: Int = -1
    private var channelStackAdapter: ChannelStackAdapter? = null
    var callback: Callback? = null

    private val llm by lazy {
        layoutManager as LinearLayoutManager
    }


    private val preLoadSizeProvider by lazy {
        ViewPreloadSizeProvider<Channel>()
    }


    init {
        layoutManager = LinearLayoutManager(context)
        itemAnimator = null
    }

    fun setupChannels(channels: List<Channel>) {

        // since we're reversed the layout, we need to reverse the channels to maintain the order
        channelStackAdapter =
            ChannelStackAdapter(context, preLoadSizeProvider, channels.reversed().toMutableList())
        setupPreloading()

        this.adapter = channelStackAdapter

        // Scroll to active middle item
        llm.reverseLayout = true

        currentViewPosition = getCurrentIndexInMiddle()

        // At this point, both view position are same,because channelUp/Down didn't happen
        prevViewPosition = currentViewPosition

        // Scrolling to mid position
        llm.scrollToPositionWithOffset(currentViewPosition, 0)

    }

    /**
     * Setup Glide preloading
     */
    private fun setupPreloading() {

        // To recycle image
        setRecyclerListener {
            channelStackAdapter?.glideRequests?.clear(
                (it as ChannelStackAdapter.ViewHolder).binding.ivChannelLogo
            )
        }

        val preLoader = RecyclerViewPreloader(
            Glide.with(this),
            channelStackAdapter!!,
            preLoadSizeProvider,
            PRELOAD_COUNT
        )
        setItemViewCacheSize(0)
        addOnScrollListener(preLoader)
    }

    /**
     * To get nearest playing position in the middle
     *
     * @return Nearest middle position or zero if couldn't find the channel
     */
    private fun getCurrentIndexInMiddle(): Int {

        if (channelStackAdapter != null) {
            val playingChannel =
                channelStackAdapter!!.channels.indexOfFirst { it.id == getPlayingChannel()?.id }
            if (playingChannel != -1) {
                val midViewPosition = channelStackAdapter!!.itemCount / 2
                val midPosListIndex = channelStackAdapter!!.getListPositionFrom(midViewPosition)
                val midPosFirst = midViewPosition - midPosListIndex
                return midPosFirst + playingChannel
            }
        }

        return 0
    }

    private fun getPlayingChannel(): Channel? {
        return channelStackAdapter?.channels?.find { it.isPlaying }
    }

    fun channelUp() {
        prevViewPosition = currentViewPosition
        llm.scrollToPositionWithOffset(++currentViewPosition, 0)
        updateAdapter()
        fireChannelChanged()
    }

    fun channelDown() {
        prevViewPosition = currentViewPosition
        llm.scrollToPositionWithOffset(--currentViewPosition, 0)
        updateAdapter()
        fireChannelChanged()
    }


    private fun updateAdapter() {
        val activeChannel = getActiveChannelFromUI()
        val prevChannel = channelStackAdapter!!.channels[channelStackAdapter!!.getListPositionFrom(
            prevViewPosition
        )]

        // Update model first
        activeChannel!!.isActive = true
        prevChannel.isActive = false

        // Now update UI
        channelStackAdapter!!.notifyItemChanged(prevViewPosition)
        channelStackAdapter!!.notifyItemChanged(currentViewPosition)
    }

    /**
     * To get active channel (focused channel) from UI
     *
     * @return Channel
     */
    private fun getActiveChannelFromUI(): Channel? {
        return channelStackAdapter?.channels?.get(
            channelStackAdapter?.getListPositionFrom(
                currentViewPosition
            ) ?: 0
        )
    }

    fun getActiveChannel(): Channel? {
        return channelStackAdapter?.channels?.find { it.isActive }
    }

    fun fireChannelChanged() {
        getActiveChannel()?.let {
            callback?.onChannelChanged(it)
        }
    }

    interface Callback {
        fun onChannelChanged(channel: Channel)
    }

}