package com.example.appfood.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.appfood.presentation.ui.adapter.BannerAdapter
import com.example.appfood.presentation.ui.adapter.CategoryAdapter
import com.example.appfood.presentation.ui.adapter.FoodAdapter
import com.example.appfood.databinding.ActivityMainBinding
import com.example.appfood.domain.model.Category
import com.example.appfood.domain.model.Food
import com.example.appfood.presentation.viewmodel.MainViewModel
import com.example.appfood.presentation.viewmodel.CartViewModel
import com.example.appfood.util.Constants
import com.example.appfood.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var bannerAdapter: BannerAdapter
    
    private var foodList = mutableListOf<Food>()
    private var categoryList = mutableListOf<Category>()
    private val db = FirebaseFirestore.getInstance()

    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        if (::binding.isInitialized && ::bannerAdapter.isInitialized) {
            var currentItem = binding.viewPagerBanner.currentItem
            currentItem++
            if (currentItem >= bannerAdapter.itemCount) {
                currentItem = 0
            }
            binding.viewPagerBanner.setCurrentItem(currentItem, true)
        }
    }

    private val mainViewModel: MainViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCategories() 
        setupBanner()
        setupRecyclerViews()
        setupSearch()
        setupCart()
        setupBottomNav()
        setupAdminFab()
        
        // Cập nhật: Lấy dữ liệu REALTIME (Tự động hiện món mới ngay khi thêm)
        observeFoodsFromFirestore()
    }

    private fun setupAdminFab() {
        // Luôn kiểm tra lại mỗi khi vào trang chủ
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.email == "admin@gmail.com") {
            binding.fabAddFoodAdmin.visibility = View.VISIBLE
            binding.fabAddFoodAdmin.setOnClickListener {
                startActivity(Intent(this, AddFoodActivity::class.java))
            }
        } else {
            binding.fabAddFoodAdmin.visibility = View.GONE
        }
    }

    private fun initCategories() {
        categoryList.clear()
        categoryList.add(Category(0, "Tất cả", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=200"))
        categoryList.add(Category(4, "Gà Rán", "https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=200"))
        categoryList.add(Category(2, "Burger", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200"))
        categoryList.add(Category(5, "Mì Ý", "https://images.unsplash.com/photo-1516100882582-96c3a05fe590?w=200"))
        categoryList.add(Category(7, "Cơm", "https://images.unsplash.com/photo-1512058560366-cd2427ffad74?w=200"))
        categoryList.add(Category(6, "Ăn kèm", "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=200"))
        categoryList.add(Category(3, "Đồ uống", "https://images.unsplash.com/photo-1544145945-f904253d0c71?w=200"))
        categoryList.add(Category(8, "Tráng miệng", "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=200"))
    }

    private fun observeFoodsFromFirestore() {
        // Sử dụng addSnapshotListener để theo dõi thay đổi liên tục
        db.collection("Foods").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Toast.makeText(this, "Lỗi kết nối dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshots != null) {
                foodList.clear()
                for (doc in snapshots) {
                    val food = doc.toObject(Food::class.java)
                    foodList.add(food)
                }
                foodAdapter.updateList(foodList)
            }
        }
    }

    private fun setupBanner() {
        bannerAdapter = BannerAdapter(Constants.BANNER_IMAGES)
        binding.viewPagerBanner.adapter = bannerAdapter
        
        binding.viewPagerBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, Constants.SLIDER_DELAY)
            }
        })
    }

    private fun setupRecyclerViews() {
        categoryAdapter = CategoryAdapter(categoryList) { category ->
            val filteredList = if (category.id == 0) foodList else foodList.filter { it.categoryId == category.id }
            foodAdapter.updateList(filteredList)
        }
        binding.recyclerViewCategory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewCategory.adapter = categoryAdapter

        foodAdapter = FoodAdapter(mutableListOf()) { food ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("food", food)
            startActivity(intent)
        }
        binding.recyclerViewFood.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewFood.adapter = foodAdapter
        
        binding.recyclerViewCategory.isNestedScrollingEnabled = false
        binding.recyclerViewFood.isNestedScrollingEnabled = false
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                val filteredList = foodList.filter { it.title.contains(query, ignoreCase = true) }
                foodAdapter.updateList(filteredList)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupCart() {
        binding.fabCart.setOnClickListener {
             startActivity(Intent(this, CartActivity::class.java))
        }

        lifecycleScope.launch {
            cartViewModel.cartSummary.collect { summary ->
                updateCartBadge(summary.first)
            }
        }
    }

    private fun updateCartBadge(itemCount: Int) {
        binding.tvCartBadge.apply {
            if (itemCount > 0) {
                text = if (itemCount > 99) "99+" else itemCount.toString()
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_order -> {
                    startActivity(Intent(this, OrderHistoryActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        // Kiểm tra lại quyền admin mỗi khi quay lại trang chủ (trường hợp vừa đăng nhập xong)
        setupAdminFab()
        sliderHandler.postDelayed(sliderRunnable, Constants.SLIDER_DELAY)
    }
}