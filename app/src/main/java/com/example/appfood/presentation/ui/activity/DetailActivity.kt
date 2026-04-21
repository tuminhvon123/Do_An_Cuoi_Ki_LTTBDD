package com.example.appfood.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.appfood.databinding.ActivityDetailBinding
import com.example.appfood.domain.model.Food
import com.example.appfood.presentation.adapter.FoodReviewAdapter
import com.example.appfood.presentation.viewmodel.CartViewModel
import com.example.appfood.presentation.viewmodel.DetailViewModel
import com.example.appfood.util.Extensions.formatCurrency
import com.example.appfood.util.Extensions.showNotification
import com.example.appfood.R
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val cartViewModel: CartViewModel by viewModels()
    private val detailViewModel: DetailViewModel by viewModels()
    private var currentFood: Food? = null
    private lateinit var reviewAdapter: FoodReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nhận dữ liệu Food từ Intent được gửi từ màn hình trước
        val food = intent.getSerializableExtra("food") as? Food
        currentFood = food

        setupRecyclerView()
        setupObservers()

        food?.let {
            displayFoodDetail(it)
            // Load các đánh giá của món ăn này từ Firestore
            detailViewModel.loadFoodRatings(it.id)
            detailViewModel.loadAverageRating(it.id)
            detailViewModel.loadRatingCount(it.id)
        }

        setupButtons()
        setupCartObserver()
        checkAdminRole()
    }

    private fun setupRecyclerView() {
        reviewAdapter = FoodReviewAdapter()
        binding.recyclerReviews.apply {
            layoutManager = LinearLayoutManager(this@DetailActivity)
            adapter = reviewAdapter
        }
    }

    private fun setupObservers() {
        // Lắng nghe danh sách đánh giá từ ViewModel
        lifecycleScope.launch {
            detailViewModel.ratings.collect { ratings ->
                if (ratings.isEmpty()) {
                    binding.recyclerReviews.visibility = View.GONE
                    binding.tvNoReviews.visibility = View.VISIBLE
                } else {
                    binding.recyclerReviews.visibility = View.VISIBLE
                    binding.tvNoReviews.visibility = View.GONE
                    reviewAdapter.submitList(ratings)
                }
            }
        }

        // Lắng nghe điểm đánh giá trung bình
        lifecycleScope.launch {
            detailViewModel.averageRating.collect { avg ->
                if (avg > 0) {
                    binding.ratingBarAverage.visibility = View.VISIBLE
                    binding.ratingBarAverage.rating = avg
                } else {
                    binding.ratingBarAverage.visibility = View.GONE
                }
            }
        }

        // Lắng nghe tổng số lượng đánh giá
        lifecycleScope.launch {
            detailViewModel.ratingCount.collect { count ->
                if (count > 0) {
                    binding.tvRatingCount.visibility = View.VISIBLE
                    binding.tvRatingCount.text = "($count đánh giá)"
                } else {
                    binding.tvRatingCount.visibility = View.GONE
                }
            }
        }
    }

    private fun checkAdminRole() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.email == "admin@gmail.com") {
            binding.btnEditFood.visibility = View.VISIBLE
            binding.btnEditFood.setOnClickListener {
                val intent = Intent(this, AddFoodActivity::class.java)
                intent.putExtra("food", currentFood)
                startActivity(intent)
            }
        } else {
            binding.btnEditFood.visibility = View.GONE
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }

        // Click vào icon giỏ hàng ở góc phải để mở màn hình CartActivity
        binding.btnCartDetail.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun setupCartObserver() {
        // Lắng nghe sự thay đổi của giỏ hàng để cập nhật Badge (số lượng) hiển thị trên icon giỏ hàng
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
                // Bước 1: Gọi hàm addToCart trong CartViewModel để lưu món ăn vào SharedPreferences/Database
                cartViewModel.addToCart(food, quantity = 1, topping = food.topping)
                
                // Bước 2: Hiển thị thông báo In-App Notification (trượt từ trên xuống) để xác nhận
                showNotification("Đã thêm ${food.title} vào giỏ hàng", isTop = true)
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
            
        // Kiểm tra trạng thái món ăn để hiển thị nút đặt hàng phù hợp
        if (food.isSoldOut) {
            binding.btnAddToCart.isEnabled = false
            binding.btnAddToCart.text = "HẾT HÀNG"
            binding.btnAddToCart.setBackgroundColor(android.graphics.Color.GRAY)
        } else {
            binding.btnAddToCart.isEnabled = true
            binding.btnAddToCart.text = "Thêm vào giỏ hàng"
            binding.btnAddToCart.setBackgroundColor(getColor(R.color.teal_primary))
        }
    }
}