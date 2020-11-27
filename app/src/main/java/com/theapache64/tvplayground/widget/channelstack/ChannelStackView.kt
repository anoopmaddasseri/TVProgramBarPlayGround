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
        // Glide channel icon preload count
        private const val PRELOAD_COUNT = 50
        private const val OFFSET_DEFAULT = 0
    }

    // Focused channel state
    private var prevViewPosition: Int = NO_POSITION
    private var currentViewPosition: Int = NO_POSITION

    // Playing channel state
    private var currentPlayingPosition: Int = NO_POSITION
    private var prevPlayingPosition: Int = NO_POSITION

    // Channel events
    var onChannelChange: OnChannelChange? = null

    // Component init goes here 👇
    private var channelStackAdapter: ChannelStackAdapter? = null
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

    /**
     * To get active channel (focused channel)
     */
    fun getActiveChannel(): Channel? {
        return channelStackAdapter?.channels?.find { it.isActive }
    }

    /**
     * To get active channel (focused channel) from UI
     */
    private fun getActiveChannelFromUI(): Channel? {
        return channelStackAdapter?.channels?.get(
            channelStackAdapter?.getListPositionFrom(
                currentViewPosition
            ) ?: OFFSET_DEFAULT
        )
    }

    /**
     * To get currently playing channel
     */
    private fun getPlayingChannel(): Channel? {
        return channelStackAdapter?.channels?.find { it.isPlaying }
    }

    /**
     * To get playing channel (focused channel) from UI
     */
    private fun getPlayingChannelFromUI(): Channel? {
        return channelStackAdapter?.channels?.get(
            channelStackAdapter?.getListPositionFrom(
                currentPlayingPosition
            ) ?: OFFSET_DEFAULT
        )
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

        return OFFSET_DEFAULT
    }

    /**
     * To move focus to above channel
     */
    fun channelFocusUp() {
        prevViewPosition = currentViewPosition
        llm.scrollToPositionWithOffset(++currentViewPosition, OFFSET_DEFAULT)
        updateAdapterChannelFocus()
        fireChannelFocusChanged()
    }

    /**
     * To move focus to below channel
     */
    fun channelFocusDown() {
        prevViewPosition = currentViewPosition
        llm.scrollToPositionWithOffset(--currentViewPosition, OFFSET_DEFAULT)
        updateAdapterChannelFocus()
        fireChannelFocusChanged()
    }

    /**
     * Playing channel change to above
     */
    fun channelUp() {
        prevPlayingPosition = currentPlayingPosition
        llm.scrollToPositionWithOffset(++currentPlayingPosition, OFFSET_DEFAULT)
        updateAdapterPlayingChannel()
        firePlayingChannelChanged()
    }

    /**
     * Playing channel change to below
     */
    fun channelDown() {
        prevPlayingPosition = currentPlayingPosition
        llm.scrollToPositionWithOffset(--currentPlayingPosition, OFFSET_DEFAULT)
        updateAdapterPlayingChannel()
        firePlayingChannelChanged()
    }

    /**
     * Adjust channel focus state when playing channel change
     */
    private fun adjustChannelFocusState() {
        prevViewPosition = prevPlayingPosition
        currentViewPosition = currentPlayingPosition
        updateAdapterChannelFocus()
    }

    /**
     * To select currently focused / jump to channel
     */
    fun selectChannel(channel: Channel? = null) {
        // TODO: 27-11-2020 Jump to channel redirection
        if (currentPlayingPosition != currentViewPosition) {
            prevPlayingPosition = currentPlayingPosition
            // Select currently focused channel
            currentPlayingPosition = currentViewPosition
            updateAdapterPlayingChannel()
            firePlayingChannelChanged()
        }
    }

    /**
     * Called during initial channel setup
     */
    fun setupChannels(channels: List<Channel>) {
        // since we're reversed the layout, we need to reverse the channels to maintain the order
        channelStackAdapter =
            ChannelStackAdapter(context, preLoadSizeProvider, channels.reversed().toMutableList())
        setupPreloading()

        adapter = channelStackAdapter

        // Scroll to active middle item
        llm.reverseLayout = true

        currentViewPosition = getCurrentIndexInMiddle()

        // At this point, both view position are same, because channelUp/Down didn't happen
        currentPlayingPosition = currentViewPosition

        // Scrolling to mid position
        llm.scrollToPositionWithOffset(currentViewPosition, OFFSET_DEFAULT)
    }

    /**
     * Called to change the focused channel
     */
    private fun updateAdapterChannelFocus() {
        val activeChannel = getActiveChannelFromUI()
        val prevChannel = channelStackAdapter!!.channels[channelStackAdapter!!.getListPositionFrom(
            prevViewPosition
        )]

        // Update model first
        activeChannel?.isActive = true
        prevChannel.isActive = false

        // Now update UI
        if (prevViewPosition > NO_POSITION) {
            channelStackAdapter?.notifyItemChanged(prevViewPosition)
        }
        channelStackAdapter?.notifyItemChanged(currentViewPosition)
    }

    /**
     * Called to change the playing channel
     */
    private fun updateAdapterPlayingChannel() {
        val playingChannel = getPlayingChannelFromUI()
        val prevPlayingChannel = channelStackAdapter?.channels?.get(
            channelStackAdapter!!.getListPositionFrom(
                prevPlayingPosition
            )
        )

        // Update model first
        playingChannel?.isPlaying = true
        prevPlayingChannel?.isPlaying = false

        // Now update UI
        if (prevPlayingPosition > NO_POSITION) {
            channelStackAdapter?.notifyItemChanged(prevPlayingPosition)
        }
        channelStackAdapter?.notifyItemChanged(currentPlayingPosition)
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

    private fun fireChannelFocusChanged() {
        getActiveChannel()?.let {
            onChannelChange?.onChannelFocusChange(it)
        }
    }

    private fun firePlayingChannelChanged() {
        getPlayingChannel()?.let {
            onChannelChange?.onPlayingChannelChange(it)
        }
    }

    interface OnChannelChange {
        /**
         * Invoked when channel changed using DPAD Up/Down
         */
        fun onChannelFocusChange(channel: Channel)

        /**
         * Invoked when channel changed using CH Up/Down
         */
        fun onPlayingChannelChange(channel: Channel)
    }
}