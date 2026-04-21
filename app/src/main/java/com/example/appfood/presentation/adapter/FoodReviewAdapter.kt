package com.example.appfood.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appfood.databinding.ItemFoodReviewBinding
import com.example.appfood.domain.model.FoodRating
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodReviewAdapter : ListAdapter<FoodRating, FoodReviewAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    inner class ReviewViewHolder(private val binding: ItemFoodReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(rating: FoodRating) {
            binding.apply {
                ratingBar.rating = rating.rating
                txtFeedback.text = if (rating.feedback.isNotEmpty()) rating.feedback else "Không có nhận xét"
                txtUserName.text = rating.getMaskedEmail()

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                txtDate.text = sdf.format(Date(rating.createdAt))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemFoodReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReviewDiffCallback : DiffUtil.ItemCallback<FoodRating>() {
        override fun areItemsTheSame(oldItem: FoodRating, newItem: FoodRating): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FoodRating, newItem: FoodRating): Boolean =
            oldItem == newItem
    }
}
