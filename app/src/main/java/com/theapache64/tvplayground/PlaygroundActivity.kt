package com.theapache64.tvplayground

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.theapache64.tvplayground.databinding.ActivityPlaygroundBinding
import com.theapache64.tvplayground.widget.channelstack.Channel
import com.theapache64.tvplayground.widget.channelstack.ChannelStackView
import com.theapache64.tvplayground.widget.channelstack.ChannelStackView.StateChStack.STATE_CH_STACK_VISIBLE
import timber.log.Timber

class PlaygroundActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaygroundBinding

    private val chStack by lazy {
        binding.channelStack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())

        binding = DataBindingUtil.setContentView(this, R.layout.activity_playground)

        val viewModel = ViewModelProvider(this).get(PlaygroundViewModel::class.java)
        binding.viewmModel = viewModel

        attachObservers(viewModel)
        attachCallbacks()
    }

    private fun attachCallbacks() {
        chStack.onChannelChange = object : ChannelStackView.OnChannelChange {
            // Invoked when channel changed using DPAD Up/Down
            override fun onChannelFocusChange(channel: Channel) {
                Timber.d("onChannelChanged: Channel changed to ${channel.imageUrl}")
            }

            // Invoked when channel changed using CH Up/Down
            override fun onPlayingChannelChange(channel: Channel) {
                Timber.d("OnPlayingChannelChange: Playing Channel changed to ${channel.imageUrl}")
            }
        }
    }

    private fun attachObservers(viewModel: PlaygroundViewModel) {
        viewModel.fakeChannels.observe(this, { channels ->
            Timber.d("onCreate: Found ${channels.size} channels")
            chStack.setupChannels(channels)
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Timber.d("onKeyDown: $keyCode")
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                chStack.channelFocusUp()
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (chStack.currentState == STATE_CH_STACK_VISIBLE) {
                    chStack.channelFocusDown()
                } else {
                    chStack.show()
                }
            }

            KeyEvent.KEYCODE_ENTER -> {
                Timber.d("onKeyDown: Launch ${chStack.getActiveChannel()}")
                chStack.selectFocusedChannel()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Timber.d("keyCode: $keyCode")
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

}