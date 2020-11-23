package com.theapache64.tvplayground

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.theapache64.tvplayground.widget.channelstack.Channel

/**
 * Created by theapache64 : Nov 20 Fri,2020 @ 19:08
 */
class PlaygroundViewModel : ViewModel() {
    private val _fakeChannels = MutableLiveData<List<Channel>>()
    val fakeChannels: LiveData<List<Channel>> = _fakeChannels

    init {
        _fakeChannels.value = mutableListOf<Channel>().apply {
            repeat(30) {
                add(
                    Channel(
                        "id-$it",
                        it,
                        "https://picsum.photos/id/1$it/200/300",
                        false,
                        false
                    )
                )
            }
        }
    }
}