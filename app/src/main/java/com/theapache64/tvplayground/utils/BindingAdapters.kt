package com.theapache64.tvplayground.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * Created by theapache64 : Nov 23 Mon,2020 @ 10:25
 */
@BindingAdapter("imageUrl")
fun loadImage(imageView: ImageView, url: String?) {
    if (url != null) {
        val requestOption = RequestOptions()

        Glide.with(imageView.context)
            .load(url)
            .apply(requestOption)
            .into(imageView)
    }
}
