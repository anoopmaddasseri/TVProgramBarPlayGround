package com.theapache64.tvplayground.widget.programbar

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.theapache64.tvplayground.R
import com.theapache64.tvplayground.databinding.ItemProgramBarBinding
import com.theapache64.tvplayground.utils.GlideApp
import com.theapache64.tvplayground.utils.GlideRequest

/**
 * Created by Anoop Maddasseri : Nov 29 Sun,2020 @ 09:17
 */
class ProgramBarAdapter(
    private val context: Context,
    private val preloadSizeProvider: ViewPreloadSizeProvider<Program>,
    val programs: MutableList<Program>,
    var onProgramSelected: (position: Int, program: Program) -> Unit
) : RecyclerView.Adapter<ProgramBarAdapter.ViewHolder>(),
    ListPreloader.PreloadModelProvider<Program> {

    companion object {
        private const val THUMB_SIZE = 20
    }

    private val layoutInflater by lazy { LayoutInflater.from(context) }

    // Glide things
    private var fullRequest: GlideRequest<Drawable>
    private var thumbRequest: GlideRequest<Drawable>
    val glideRequests = GlideApp.with(context)

    init {
        // Creating glide request managers
        fullRequest = glideRequests
            .asDrawable()

        thumbRequest = glideRequests.asDrawable()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .override(THUMB_SIZE)
            .priority(Priority.HIGH)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemProgramBarBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        ).apply {
            binding.clProgramBar.onFocusChangeListener = focusChangeListener
            // Informing glide pre-loader about the viewport size
            preloadSizeProvider.setView(binding.ivProgramLogo)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, viewPosition: Int) {
        val program = programs[holder.layoutPosition]
        holder.bind(holder.layoutPosition, program)
    }

    inner class ViewHolder(val binding: ItemProgramBarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pos: Int, program: Program) {
            binding.program = program

            // Load image
            fullRequest.load(program.imageUrl)
                .thumbnail(thumbRequest.load(program.imageUrl))
                .into(binding.ivProgramLogo)

            binding.clProgramBar.setOnClickListener {
                onProgramSelected(pos, program)
            }
        }

        fun onFocusRequestReceived() {
            binding.clProgramBar.requestFocus()
        }
    }

    private val focusChangeListener =
        View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                runScaleAnimation(view)
            } else {
                runScaleAnimation(view, false)
            }
        }

    override fun getPreloadItems(viewPosition: Int): MutableList<Program> {
        return mutableListOf(programs[viewPosition])
    }

    override fun getPreloadRequestBuilder(item: Program): RequestBuilder<*>? {
        return fullRequest
            .thumbnail(thumbRequest.load(item.imageUrl))
            .load(item.imageUrl)
    }

    override fun getItemCount() = programs.count()

    fun onFocusRequestReceived(vh: RecyclerView.ViewHolder?) {
        (vh as? ViewHolder)?.onFocusRequestReceived()
    }

    private fun runScaleAnimation(view: View, scaleIn: Boolean = true) {
        if (scaleIn) {
            // run scale animation and make it bigger
            val scaleInAnim =
                AnimationUtils.loadAnimation(context, R.anim.scale_in) as ScaleAnimation
            view.startAnimation(scaleInAnim)
            scaleInAnim.fillAfter = true
        } else {
            // run scale animation and make it smaller
            val scaleOutAnim =
                AnimationUtils.loadAnimation(context, R.anim.scale_out) as ScaleAnimation
            view.startAnimation(scaleOutAnim)
            scaleOutAnim.fillAfter = true
        }
    }
}