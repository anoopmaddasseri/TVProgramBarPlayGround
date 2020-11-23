package com.theapache64.tvplayground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.theapache64.tvplayground.databinding.ActivityPlaygroundBinding
import timber.log.Timber

class PlaygroundActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())

        val binding = DataBindingUtil.setContentView<ActivityPlaygroundBinding>(
            this,
            R.layout.activity_playground
        )

        val viewModel = ViewModelProvider(this).get(PlaygroundViewModel::class.java)
        binding.viewmModel = viewModel

        viewModel.fakeChannels.observe(this, { channels ->
            Timber.d("onCreate: Found ${channels.size} channels")
            binding.channelStack.setChannels(channels)
        })

    }
}