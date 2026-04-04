package com.example.appfood.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appfood.R
import com.example.appfood.databinding.ItemCartBinding
import com.example.appfood.domain.model.CartItem
import com.example.appfood.util.PriceFormatter

class CartAdapter : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartItemDiffCallback()) {

    var onQuantityChanged: ((CartItem, Int) -> Unit)? = null
    var onItemRemoved: ((CartItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.apply {
                tvFoodTitle.text = cartItem.foodTitle
                
                if (cartItem.topping.isNotEmpty()) {
                    tvTopping.text = "Topping: ${cartItem.topping}"
                    tvTopping.visibility = View.VISIBLE
                } else {
                    tvTopping.visibility = View.GONE
                }

                tvPrice.text = formatPrice(cartItem.foodPrice)
                tvQuantity.text = cartItem.quantity.toString()

                Glide.with(root.context)
                    .load(cartItem.foodImageUrl)
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.error_food)
                    .centerCrop()
                    .into(ivFoodImage)

                btnIncrease.setOnClickListener {
                    val newQuantity = cartItem.quantity + 1
                    onQuantityChanged?.invoke(cartItem, newQuantity)
                }

                btnDecrease.setOnClickListener {
                    val newQuantity = cartItem.quantity - 1
                    onQuantityChanged?.invoke(cartItem, newQuantity)
                }

                btnRemove.setOnClickListener {
                    onItemRemoved?.invoke(cartItem)
                }
            }
        }

        private fun formatPrice(price: Double): String {
            return PriceFormatter.formatPrice(price)
        }
    }

    private class CartItemDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
