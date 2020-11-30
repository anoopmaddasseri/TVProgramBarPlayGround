package com.theapache64.tvplayground.widget.programbar

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.core.view.isVisible
import androidx.leanback.widget.HorizontalGridView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper

/**
 * Created by Anoop Maddasseri : Nov 29 Sun,2020 @ 09:17
 */
class ProgramBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : HorizontalGridView(context, attrs, defStyleAttr) {

    companion object {
        // Glide program icon preload count
        private const val PRELOAD_COUNT = 50
        private const val OFFSET_DEFAULT = 0

        // Stack show / hide
        private const val FADE_IN_DURATION = 500L
        private const val FADE_OUT_DURATION = 200L
    }

    // Focused program state
    private var prevViewPosition: Int = NO_POSITION
    private var currentViewPosition: Int = NO_POSITION

    // Playing program state
    private var currentPlayingPosition: Int = NO_POSITION
    private var prevPlayingPosition: Int = NO_POSITION

    // Program events
    var onProgramChange: OnProgramChange? = null

    // Program stack state
    var currentState = StateProgramStack.STATE_PGM_STACK_GONE

    enum class StateProgramStack {
        STATE_PGM_STACK_VISIBLE,
        STATE_PGM_STACK_GONE
    }

    // Component init goes here 👇
    private var programBarAdapter: ProgramBarAdapter? = null
    private val llm by lazy {
        layoutManager as LinearLayoutManager
    }
    private val preLoadSizeProvider by lazy {
        ViewPreloadSizeProvider<Program>()
    }

    init {
        itemAnimator = null

        // Snap item center always
        val snapHelper: SnapHelper = GravitySnapHelper(Gravity.CENTER)
        snapHelper.attachToRecyclerView(this)
    }

    /**
     * To get active program (focused program)
     */
    fun getActiveProgram(): Program? {
        return programBarAdapter?.programs?.find { it.isActive }
    }

    /**
     * To get active program (focused program) from UI
     */
    private fun getActiveProgramFromUI(): Program? {
        return programBarAdapter?.programs?.get(currentViewPosition)
    }

    /**
     * To get currently playing program
     */
    private fun getPlayingProgram(): Program? {
        return programBarAdapter?.programs?.find { it.isPlaying }
    }

    /**
     * To get middle program index
     *
     */
    private fun getCurrentIndexInMiddle(): Int {
        if (programBarAdapter != null) {
            val playingProgramIndex =
                programBarAdapter!!.programs.size.div(2)
            if (playingProgramIndex != NO_POSITION) return playingProgramIndex
        }
        return OFFSET_DEFAULT
    }

    /**
     * To move focus to next program.
     */
    fun moveToNextProgram() {
        if (canMoveNext()) {
            prevViewPosition = currentViewPosition
            scrollToPosition(++currentViewPosition)
            updateAdapterProgramFocus()
            fireProgramFocusChanged()
        }
    }

    private fun canMoveNext() =
        currentViewPosition.inc() < programBarAdapter?.itemCount ?: OFFSET_DEFAULT

    /**
     * To move focus to previous program.
     */
    fun moveToPrevProgram() {
        if (canMovePrev()) {
            prevViewPosition = currentViewPosition
            scrollToPosition(--currentViewPosition)
            updateAdapterProgramFocus()
            fireProgramFocusChanged()
        }
    }

    /**
     * Gain focus back to program bar
     */
    private fun gainFocus() {
        if (hasFocus().not()) requestChildFocus()
    }

    private fun canMovePrev() =
        currentViewPosition > OFFSET_DEFAULT

    /**
     * To select currently focused program
     */
    private fun selectFocusedProgram(pos: Int, program: Program) {
        if (prevPlayingPosition != pos) {
            prevPlayingPosition = currentPlayingPosition
            // Select currently focused program
            currentPlayingPosition = pos
            updateAdapterPlayingChannel(program)
            firePlayingProgramChanged()
        }
    }

    /**
     * To show program stack
     */
    fun show() {
        isVisible = true
        animate().cancel()
        animate().alpha(1f).duration = FADE_IN_DURATION
        currentState = StateProgramStack.STATE_PGM_STACK_VISIBLE
        gainFocus()
    }

    /**
     * To hide program stack
     */
    fun hide() {
        isVisible = false
        animate().cancel()
        animate().alpha(0f).duration = FADE_OUT_DURATION
        animate().withEndAction {
            isVisible = false
            currentState = StateProgramStack.STATE_PGM_STACK_GONE
        }
    }

    /**
     * To toggle program stack visibility
     */
    fun toggle() {
        if (isVisible) hide() else show()
    }

    /**
     * Jump to program
     */
    fun selectProgram(program: Program) {
        TODO("Jump to program")
    }

    /**
     * Triggered when selecting a program
     */
    private fun onProgramSelected(): (Int, program: Program) -> Unit =
        { pos: Int, pgm -> selectFocusedProgram(pos, pgm) }

    /**
     * Called during initial program setup
     */
    fun setupPrograms(programs: List<Program>) {
        // since we're reversed the layout, we need to reverse the channels to maintain the order
        programBarAdapter =
            ProgramBarAdapter(
                context,
                preLoadSizeProvider,
                programs.toMutableList(),
                onProgramSelected = onProgramSelected()
            )

        currentViewPosition = getCurrentIndexInMiddle()

        // Active middle program TODO: 29-11-2020 'On Now' program whilst actual implementation
        if (currentViewPosition > OFFSET_DEFAULT) {
            programBarAdapter!!.programs[currentViewPosition].isActive = true
        }

        // At this point, both view position are same, because channelUp/Down didn't happen
        currentPlayingPosition = currentViewPosition
        // TODO: 30-11-2020 Grid layout manager preloading setup
        // setupPreloading()
        adapter = programBarAdapter

        // Scrolling to mid position
        scrollToPosition(currentViewPosition)
        requestChildFocus()
    }

    /**
     * Called to change the focused program
     */
    private fun updateAdapterProgramFocus() {
        val activeProgram = getActiveProgramFromUI()
        val prevProgram = programBarAdapter!!.programs[prevViewPosition]

        activeProgram?.isActive = true
        prevProgram.isActive = false

        // Prev. pgm state update
        if (prevViewPosition > NO_POSITION) {
            programBarAdapter?.notifyItemChanged(prevViewPosition)
        }

        programBarAdapter?.notifyItemChanged(currentViewPosition)
    }

    /**
     * Called to change the playing program
     */
    private fun updateAdapterPlayingChannel(activeProgram: Program) {
        val prevProgram = programBarAdapter!!.programs[prevPlayingPosition]

        // Update model first
        activeProgram.isPlaying = true
        prevProgram.isPlaying = false

        // Now update UI
        if (prevPlayingPosition > NO_POSITION) {
            programBarAdapter?.notifyItemChanged(prevPlayingPosition)
        }
        programBarAdapter?.notifyItemChanged(currentPlayingPosition)
    }

    /**
     * Setup Glide preloading
     */
    private fun setupPreloading() {
        // To recycle image
        setRecyclerListener {
            programBarAdapter?.glideRequests?.clear(
                (it as ProgramBarAdapter.ViewHolder).binding.ivProgramLogo
            )
        }

        val preLoader = RecyclerViewPreloader(
            Glide.with(this),
            programBarAdapter!!,
            preLoadSizeProvider,
            PRELOAD_COUNT
        )
        setItemViewCacheSize(0)
        addOnScrollListener(preLoader)
    }

    /**
     * Request focus to specific vh child
     */
    fun requestChildFocus(holderPos: Int = currentViewPosition) {
        post {
            programBarAdapter!!.onFocusRequestReceived(findViewHolderForAdapterPosition(holderPos))
        }
    }

    private fun fireProgramFocusChanged() {
        getActiveProgram()?.let {
            onProgramChange?.onProgramChanged(it)
        }
    }

    private fun firePlayingProgramChanged() {
        getPlayingProgram()?.let {
            onProgramChange?.onProgramSelected(it)
        }
    }

    interface OnProgramChange {
        /**
         * Invoked when program changed using DPAD LEFT/RIGHT
         */
        fun onProgramChanged(program: Program)

        /**
         * Invoked when program selected
         */
        fun onProgramSelected(program: Program)
    }
}