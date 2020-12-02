package com.theapache64.tvplayground.widget.programbar

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.theapache64.tvplayground.databinding.ItemProgramBarBinding
import com.theapache64.tvplayground.utils.GlideApp
import com.theapache64.tvplayground.utils.GlideRequest
import com.theapache64.tvplayground.utils.runScaleAnimation

/**
 * Created by Anoop Maddasseri : Nov 29 Sun,2020 @ 09:17
 */
class ProgramBarAdapter(
    private val context: Context,
    private val preloadSizeProvider: ViewPreloadSizeProvider<Program>,
    var programs: MutableList<Program>,
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

    override fun getItemCount() = programs.count()

    /**
     * Diff util update
     */
    fun update(newSetPrograms: MutableList<Program>) {
        val diffCallback = ProgramItemDiffCallback(newSetPrograms, programs)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        programs = newSetPrograms
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(val binding: ItemProgramBarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pos: Int, program: Program) {
            binding.program = program

            // Load image
            fullRequest.load(program.imageUrl)
                .thumbnail(thumbRequest.load(program.imageUrl))
                .into(binding.ivProgramLogo)

            binding.programId.isVisible = program.isActive

            binding.clProgramBar.setOnClickListener {
                onProgramSelected(pos, program)
            }
        }

        fun clearChildFocus() {
            binding.clProgramBar.clearAnimation()
        }

        fun requestChildFocus() {
            binding.clProgramBar.runScaleAnimation()
        }
    }

    fun requestChildFocus(vh: RecyclerView.ViewHolder?) {
        (vh as? ViewHolder)?.requestChildFocus()
    }

    fun clearChildFocus(vh: RecyclerView.ViewHolder?) {
        (vh as? ViewHolder)?.clearChildFocus()
    }

    private val focusChangeListener =
        View.OnFocusChangeListener { view, hasFocus ->
            view.clearAnimation()
            if (hasFocus) {
                view.runScaleAnimation()
            }
        }

    inner class ProgramItemDiffCallback(
        private val newItems: List<Program>,
        private val oldItems: List<Program>
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]
            return oldItem.id == newItem.id
        }

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]
            return oldItem.startAt == newItem.startAt && oldItem.endAt == newItem.startAt
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

}