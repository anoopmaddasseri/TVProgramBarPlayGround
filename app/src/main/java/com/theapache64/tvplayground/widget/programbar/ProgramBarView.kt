package com.theapache64.tvplayground.widget.programbar

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import androidx.leanback.widget.HorizontalGridView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.theapache64.tvplayground.R
import timber.log.Timber

/**
 * Created by Anoop Maddasseri : Nov 29 Sun,2020 @ 09:17
 */
class ProgramBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : HorizontalGridView(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "ProgramBarView"

        // Glide program icon preload count
        private const val PRELOAD_COUNT = 50
        private const val OFFSET_DEFAULT = 0

        // Stack show / hide
        private const val FADE_IN_DURATION = 500L
        private const val FADE_OUT_DURATION = 200L

        // Activity capture
        private const val DELAY_ACTIVITY_INTERCEPT = 100L

        // Paging trigger threshold
        private const val PAGING_TRIGGER_THRESHOLD = 2
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

    // Program paging events
    var onPagingStateChange: PagingStateChange? = null

    // Program paging state
    var currentPgmPagingState = StatePgmPaging.STATE_PAGING_NONE

    // Interception
    private var activityCaptureTime = 0L

    // Misc
    private val mPrograms: List<Program>?
        get() = programBarAdapter?.programs

    private var mTargetPlayingCh = true

    private val actualPlayingPos: Int
        get() = if (currentPlayingPosition > NO_POSITION && mTargetPlayingCh) {
            currentPlayingPosition
        } else {
            getCurrentIndexInMiddle()
        }

    enum class StateProgramStack {
        STATE_PGM_STACK_HINT_VISIBLE,
        STATE_PGM_STACK_VISIBLE,
        STATE_PGM_STACK_GONE
    }

    enum class StatePgmPaging {
        STATE_PAGING_NONE,
        STATE_PAGING_START,
        STATE_PAGING_END
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
    }

    /**
     * To get active program (focused program)
     */
    fun getActiveProgram(): Program? {
        return mPrograms?.find { it.isActive }
    }

    /**
     * To get active program (focused program) from UI
     */
    private fun getActiveProgramFromUI(): Program? {
        return mPrograms?.get(currentViewPosition)
    }

    /**
     * To get currently playing program
     */
    private fun getPlayingProgram(): Program? {
        return mPrograms?.find { it.isPlaying }
    }

    /**
     * To get middle program index
     *
     */
    private fun getCurrentIndexInMiddle(): Int {
        return mPrograms?.count()?.div(2) ?: OFFSET_DEFAULT
    }

    /**
     * To move focus to next program.
     */
    fun moveToNextProgram() {
        if (canMoveNext()) {
            activityCaptureTime = System.currentTimeMillis()
            prevViewPosition = currentViewPosition
            scrollToPosition(++currentViewPosition)
            updateAdapterProgramFocus()
            fireProgramFocusChanged()

            // Paging state change based on the threshold
            val pagingThreshPos = mPrograms?.count()?.minus(PAGING_TRIGGER_THRESHOLD)
            firePagingStateChanged(pagingThreshPos, StatePgmPaging.STATE_PAGING_END)
        }
    }

    /**
     * To move focus to previous program.
     */
    fun moveToPrevProgram() {
        if (canMovePrev()) {
            activityCaptureTime = System.currentTimeMillis()
            prevViewPosition = currentViewPosition
            scrollToPosition(--currentViewPosition)
            updateAdapterProgramFocus()
            fireProgramFocusChanged()

            // Paging state change based on the threshold
            firePagingStateChanged(PAGING_TRIGGER_THRESHOLD, StatePgmPaging.STATE_PAGING_START)
        }
    }

    private fun canMoveNext() =
        currentViewPosition.inc() < mPrograms?.count() ?: OFFSET_DEFAULT && allowKeyEvent() && currentState == StateProgramStack.STATE_PGM_STACK_VISIBLE

    private fun canMovePrev() =
        currentViewPosition > OFFSET_DEFAULT && allowKeyEvent() && currentState == StateProgramStack.STATE_PGM_STACK_VISIBLE

    private fun allowKeyEvent(): Boolean {
        val allowKeyEvent =
            System.currentTimeMillis().minus(activityCaptureTime) > DELAY_ACTIVITY_INTERCEPT
        Timber.tag(TAG).d("allowKeyEvent : $allowKeyEvent")
        return allowKeyEvent
    }

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
     * Scroll to currently playing program
     */
    fun scrollToPlayingProgram() {
        if (currentPlayingPosition > NO_POSITION) {
            scrollToPosition(currentPlayingPosition)
        }
    }

    /**
     * To show program stack
     */
    fun show() {
        isVisible = true
        animate().cancel()
        animate().alpha(1f).duration = FADE_IN_DURATION
        if (currentState == StateProgramStack.STATE_PGM_STACK_HINT_VISIBLE) {
            translationY = 0f
            currentState = StateProgramStack.STATE_PGM_STACK_VISIBLE
            requestChildFocus()
        } else {
            translationY = resources.getDimension(R.dimen.program_bar_hint_translation)
            currentState = StateProgramStack.STATE_PGM_STACK_HINT_VISIBLE
            clearChildFocus()
        }
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
        programBarAdapter =
            ProgramBarAdapter(
                context,
                preLoadSizeProvider,
                programs.toMutableList(),
                onProgramSelected = onProgramSelected()
            )
        Timber.d("States : $currentViewPosition | $currentPlayingPosition | $actualPlayingPos | $mTargetPlayingCh")
        currentViewPosition = actualPlayingPos

        // Active middle program TODO: 29-11-2020 'On Now' program whilst actual implementation
        if (currentViewPosition > OFFSET_DEFAULT) {
            mPrograms!![currentViewPosition].isActive = true
        }

        if (mTargetPlayingCh) {
            // At this point, both view position are same, because channelUp/Down didn't happen.
            // Don't update playing position for channel focus change
            currentPlayingPosition = currentViewPosition
        }

        adapter = programBarAdapter

        // Scrolling to mid position
        scrollToPosition(currentViewPosition)

        mTargetPlayingCh = false
    }

    /**
     * Update programs based on the current state
     */
    fun update(programs: List<Program>) {
        Timber.tag(TAG)
            .d("Paging state: $currentPgmPagingState | programs: ${programs.size}")
        var programsNewSet = mPrograms?.toMutableList()
        when (currentPgmPagingState) {
            StatePgmPaging.STATE_PAGING_START -> {
                // Programs goes to the start
                programsNewSet?.addAll(0, programs)
            }
            StatePgmPaging.STATE_PAGING_END -> {
                // Programs goes to the end
                programsNewSet?.addAll(programs)
            }
            else -> {
                // Afresh state, replace with program set
                programsNewSet = programs.toMutableList()
            }
        }

        programBarAdapter?.update(programsNewSet!!)
    }

    /**
     * Reset program bar states
     */
    fun reset(targetPlayingCh: Boolean = false) {
        currentPgmPagingState = StatePgmPaging.STATE_PAGING_NONE

        // Reset focused program state
        prevViewPosition = NO_POSITION
        currentViewPosition = NO_POSITION

        mTargetPlayingCh = targetPlayingCh
    }

    /**
     * Called to change the focused program
     */
    private fun updateAdapterProgramFocus() {
        val activeProgram = getActiveProgramFromUI()
        val prevProgram = mPrograms!![prevViewPosition]

        activeProgram?.isActive = true
        prevProgram.isActive = false
    }

    /**
     * Called to change the playing program
     */
    private fun updateAdapterPlayingChannel(activeProgram: Program) {
        val prevProgram = mPrograms!![prevPlayingPosition]

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
    private fun requestChildFocus(holderPos: Int = currentViewPosition) {
        post {
            programBarAdapter!!.requestChildFocus(findViewHolderForAdapterPosition(holderPos))
        }
    }

    /**
     * Clear focus from specific vh child
     */
    private fun clearChildFocus(holderPos: Int = currentViewPosition) {
        post {
            programBarAdapter?.clearChildFocus(findViewHolderForAdapterPosition(holderPos))
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

    private fun firePagingStateChanged(pagingThreshPos: Int?, pagingState: StatePgmPaging) {
        if (currentViewPosition == pagingThreshPos) {
            currentPgmPagingState = pagingState
            onPagingStateChange?.onPagingStateChanged(
                mPrograms!![currentViewPosition].startAt,
                pagingState
            )
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

    interface PagingStateChange {
        /**
         * Invoked when program paging state changed
         */
        fun onPagingStateChanged(startAt: Long?, state: StatePgmPaging)
    }
}