package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.appfood.databinding.ActivityDetailBinding
import com.example.appfood.domain.model.Food
import com.example.appfood.util.Extensions.formatCurrency
import com.example.appfood.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nhận dữ liệu Food từ Intent
        val food = intent.getSerializableExtra("food") as? Food

        food?.let {
            displayFoodDetail(it)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAddToCart.setOnClickListener {
            // Logic thêm vào giỏ hàng (sẽ làm sau)
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