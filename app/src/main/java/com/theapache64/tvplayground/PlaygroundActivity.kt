package com.theapache64.tvplayground

import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.theapache64.tvplayground.databinding.ActivityPlaygroundBinding
import com.theapache64.tvplayground.utils.toast
import com.theapache64.tvplayground.widget.channelstack.Channel
import com.theapache64.tvplayground.widget.channelstack.ChannelStackView
import com.theapache64.tvplayground.widget.channelstack.ChannelStackView.StateChStack
import com.theapache64.tvplayground.widget.programbar.Program
import com.theapache64.tvplayground.widget.programbar.ProgramBarView
import com.theapache64.tvplayground.widget.programbar.ProgramBarView.StateProgramStack
import timber.log.Timber

class PlaygroundActivity : AppCompatActivity() {

    // TODO: 01-12-2020 Auto hide mocking, remove
    private val chStackAutoHideMocker = Handler()
    private lateinit var binding: ActivityPlaygroundBinding

    private val chStack by lazy {
        binding.channelStack
    }

    private val programBar by lazy {
        binding.programBar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())

        binding = DataBindingUtil.setContentView(this, R.layout.activity_playground)

        val viewModel = ViewModelProvider(this).get(PlaygroundViewModel::class.java)
        binding.viewmModel = viewModel

        attachObservers(viewModel)
        attachCallbacks()

        // Request to fetch fake programs
        viewModel.fetchFakePrograms()
    }

    private fun attachCallbacks() {
        chStack.onChannelChange = object : ChannelStackView.OnChannelChange {
            // Invoked when channel changed using DPAD Up/Down
            override fun onChannelFocusChange(channel: Channel) {
                Timber.d("onChannelChanged: Channel changed to ${channel.id}")
            }

            // Invoked when channel changed using CH Up/Down
            override fun onPlayingChannelChange(channel: Channel) {
                Timber.d("OnPlayingChannelChange: Playing Channel changed to ${channel.id}")
            }
        }

        programBar.onProgramChange = object : ProgramBarView.OnProgramChange {
            override fun onProgramChanged(program: Program) {
                Timber.d("onProgramChanged: ${program.id}")
            }

            override fun onProgramSelected(program: Program) {
                Timber.d("onProgramSelected: ${program.id}")
                toast("onProgramSelected : ${program.id}")
            }
        }

        programBar.onPagingStateChange = object : ProgramBarView.PagingStateChange {
            override fun onPagingStateChanged(
                startAt: Long?,
                state: ProgramBarView.StatePgmPaging
            ) {
                toast("onPagingStateChanged : $state")
            }
        }
    }

    private fun attachObservers(viewModel: PlaygroundViewModel) {
        viewModel.fakeChannels.observe(this, { channels ->
            Timber.d("fakeChannels: Found ${channels.size} channels")
            chStack.setupChannels(channels)
        })

        viewModel.fakePrograms.observe(this, { programs ->
            Timber.d("fakePrograms: Found ${programs.size} programs")
            programBar.setupPrograms(programs)
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Timber.d("onKeyDown: $keyCode")
        scheduleChStackAutoHide()
        var shouldIntercept = false
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                chStack.channelFocusUp()
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (chStack.currentState != StateChStack.STATE_CH_STACK_VISIBLE) {
                    channelPgmStackState()
                } else {
                    chStack.channelFocusDown()
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                programBar.moveToPrevProgram()
                shouldIntercept = true
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                programBar.moveToNextProgram()
                shouldIntercept = true
            }
        }
        return shouldIntercept
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Timber.d("keyCode: $keyCode")
        scheduleChStackAutoHide()
        when (keyCode) {
            KeyEvent.KEYCODE_PAGE_UP, KeyEvent.KEYCODE_CHANNEL_UP -> {
                chStack.channelUp()
            }

            KeyEvent.KEYCODE_PAGE_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                chStack.channelDown()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun channelPgmStackState() {
        programBar.show()
        if (programBar.currentState == StateProgramStack.STATE_PGM_STACK_VISIBLE) {
            chStack.show()
            scheduleChStackAutoHide()
        }
    }

    private fun hideChannelStack() {
        programBar.hide()
        chStack.hide()
    }

    private fun scheduleChStackAutoHide() {
        chStackAutoHideMocker.removeCallbacksAndMessages(null)
        chStackAutoHideMocker.postDelayed(chStackAutoHideRun(), 4000)
    }

    private fun chStackAutoHideRun(): () -> Unit = { hideChannelStack() }
}