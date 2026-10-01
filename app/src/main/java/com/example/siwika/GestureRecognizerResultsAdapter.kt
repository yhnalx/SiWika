package com.example.siwika

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.siwika.databinding.CellGestureRecognizerResultBinding
import com.google.mediapipe.tasks.components.containers.Category
import java.util.Locale
import kotlin.math.min

class GestureRecognizerResultsAdapter : RecyclerView.Adapter<GestureRecognizerResultsAdapter.ViewHolder>() {
    companion object {
        private const val NO_VALUE = "--"
    }

    private var adapterCategories: MutableList<Category?> = mutableListOf()
    private var adapterSize: Int = 0

    @SuppressLint("NotifyDataSetChanged")
    fun updateResults(categories: List<Category>?) {
        adapterCategories = MutableList(adapterSize) { null }
        if (categories != null) {
            val sortedCategories = categories.sortedByDescending { it.score() }
            val min = min(sortedCategories.size, adapterCategories.size)
            for (i in 0 until min) {
                adapterCategories[i] = sortedCategories[i]
            }
            adapterCategories.sortedBy { it?.index() }
            notifyDataSetChanged()
        }
    }

    fun updateAdapterSize(size: Int) {
        adapterSize = size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CellGestureRecognizerResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        adapterCategories[position].let { category ->
            holder.bind(category?.categoryName(), category?.score())
        }
    }

    override fun getItemCount(): Int = adapterCategories.size

    inner class ViewHolder(private val binding: CellGestureRecognizerResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(label: String?, score: Float?) {
            with(binding) {
                // Set the Text
                tvLabel.text = label?.replaceFirstChar { it.uppercase() } ?: NO_VALUE

                // Format Score as Percentage (e.g., 95%)
                if (score != null) {
                    val percentage = (score * 100).toInt()
                    tvScore.text = "$percentage%"

                    // Update the Progress Bar (scaled to 100)
                    indicatorConfidence.setProgress(percentage, true)
                } else {
                    tvScore.text = NO_VALUE
                    indicatorConfidence.progress = 0
                }
            }
        }
    }
}