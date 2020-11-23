package com.theapache64.tvplayground

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.theapache64.tvplayground.databinding.ActivityPlaygroundBinding
import timber.log.Timber

class PlaygroundActivity : AppCompatActivity() {


    private lateinit var binding: ActivityPlaygroundBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())

        this.binding = DataBindingUtil.setContentView<ActivityPlaygroundBinding>(
            this,
            R.layout.activity_playground
        )

        val viewModel = ViewModelProvider(this).get(PlaygroundViewModel::class.java)
        binding.viewmModel = viewModel

        viewModel.fakeChannels.observe(this, { channels ->
            Timber.d("onCreate: Found ${channels.size} channels")
            binding.channelStack.setupChannels(this, channels)
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Timber.d("onKeyDown: $keyCode")
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                binding.channelStack.channelUp()
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                binding.channelStack.channelDown()
            }

            KeyEvent.KEYCODE_ENTER -> {
                Timber.d("onKeyDown: Launch ${binding.channelStack.getActiveChannelFromUI()}")
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}