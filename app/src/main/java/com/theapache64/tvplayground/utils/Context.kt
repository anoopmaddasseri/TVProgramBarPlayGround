package com.theapache64.tvplayground.utils

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.Toast
import com.theapache64.tvplayground.R

fun Context?.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun View.runScaleAnimation(lister: (() -> Unit)? = null) {
    // run scale animation and make it bigger
    val scaleInAnim =
        AnimationUtils.loadAnimation(context, R.anim.scale_in) as ScaleAnimation
    startAnimation(scaleInAnim)
    scaleInAnim.fillAfter = true
    scaleInAnim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(p0: Animation?) {
        }

        override fun onAnimationEnd(p0: Animation?) {
            lister?.invoke()
        }

        override fun onAnimationRepeat(p0: Animation?) {
        }
    })
}
