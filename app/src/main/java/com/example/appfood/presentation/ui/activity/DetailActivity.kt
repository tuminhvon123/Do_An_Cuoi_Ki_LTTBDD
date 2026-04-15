package com.example.appfood.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.appfood.databinding.ActivityDetailBinding
import com.example.appfood.domain.model.Food
import com.example.appfood.presentation.viewmodel.CartViewModel
import com.example.appfood.util.Extensions.formatCurrency
import com.example.appfood.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val cartViewModel: CartViewModel by viewModels()
    private var currentFood: Food? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nhận dữ liệu Food từ Intent
        val food = intent.getSerializableExtra("food") as? Food
        currentFood = food

        food?.let {
            displayFoodDetail(it)
        }

        setupButtons()
        setupCartObserver()
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }

        // Click vào icon giỏ hàng ở góc phải
        binding.btnCartDetail.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun setupCartObserver() {
        // Lắng nghe sự thay đổi của giỏ hàng để cập nhật Badge (số lượng)
        lifecycleScope.launch {
            cartViewModel.cartSummary.collect { summary ->
                updateCartBadge(summary.first)
            }
        }
    }

    private fun updateCartBadge(itemCount: Int) {
        binding.tvCartBadgeDetail.apply {
            if (itemCount > 0) {
                text = if (itemCount > 99) "99+" else itemCount.toString()
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun addToCart() {
        currentFood?.let { food ->
            if (!food.isSoldOut) {
                cartViewModel.addToCart(food, quantity = 1, topping = food.topping)
                Toast.makeText(this, "Đã thêm ${food.title} vào giỏ hàng", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayFoodDetail(food: Food) {
        binding.tvDetailTitle.text = food.title
        binding.tvDetailDesc.text = food.description
        
        binding.tvDetailPrice.text = food.price.formatCurrency()
        
        binding.tvTopping.text = if (food.topping.isEmpty()) "Không có topping" else food.topping

        Glide.with(this)
            .load(food.imageUrl)
            .into(binding.imgDetail)
            
        // Nếu hết hàng thì disable nút đặt hàng
        if (food.isSoldOut) {
            binding.btnAddToCart.isEnabled = false
            binding.btnAddToCart.text = "HẾT HÀNG"
            binding.btnAddToCart.setBackgroundColor(android.graphics.Color.GRAY)
        }
    }
}