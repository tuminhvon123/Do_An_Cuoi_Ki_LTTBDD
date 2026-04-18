package com.example.appfood.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
import com.example.appfood.domain.model.FoodSize
import com.example.appfood.presentation.viewmodel.MainViewModel
import com.example.appfood.presentation.viewmodel.CartViewModel
import com.example.appfood.util.Constants
import com.example.appfood.R
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
    private var bannerImages = Constants.BANNER_IMAGES

    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        if (::binding.isInitialized) {
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

        initData() 
        setupBanner()
        setupRecyclerViews()
        setupSearch()
        setupCart()
        setupBottomNav()
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
                R.id.nav_my_list -> {
                    // Chức năng My List sẽ làm sau
                    true
                }
                else -> false
            }
        }
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

    private fun setupBanner() {
        bannerAdapter = BannerAdapter(bannerImages)
        binding.viewPagerBanner.adapter = bannerAdapter
        
        binding.viewPagerBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, Constants.SLIDER_DELAY)
            }
        })
    }

    private fun initData() {
        categoryList.clear()
        categoryList.add(Category(0, "Tất cả", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=200"))
        categoryList.add(Category(4, "Gà Rán", "https://images.unsplash.com/photo-1562967914-608f82629710?w=200"))
        categoryList.add(Category(2, "Burger", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200"))
        categoryList.add(Category(5, "Mì Ý", "https://images.unsplash.com/photo-1516100882582-96c3a05fe590?w=200"))
        categoryList.add(Category(7, "Cơm", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=200"))
        categoryList.add(Category(6, "Ăn kèm", "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=200"))
        categoryList.add(Category(3, "Đồ uống", "https://images.unsplash.com/photo-1544145945-f904253d0c71?w=200"))
        categoryList.add(Category(8, "Tráng miệng", "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=200"))

        foodList.clear()
        
        // --- Gà Rán ---
        foodList.add(Food("101", "Chickenjoy 1 miếng", "1 miếng gà giòn tan đặc trưng.", 35000.0, "https://images.unsplash.com/photo-1562967914-608f82629710?w=500", 4, false, ""))
        foodList.add(Food("102", "Chickenjoy 2 miếng", "2 miếng gà giòn tan.", 65000.0, "https://images.unsplash.com/photo-1562967914-608f82629710?w=500", 4, false, ""))
        foodList.add(Food("104", "Chickenjoy bucket", "Xô gà giòn tiết kiệm.", 99000.0, "https://images.unsplash.com/photo-1562967914-608f82629710?w=500", 4, false, "",
            listOf(FoodSize("3 miếng", 0.0), FoodSize("5 miếng", 56000.0), FoodSize("10 miếng", 200000.0))))

        // --- Burger ---
        foodList.add(Food("201", "Yum Burger", "Burger bò truyền thống.", 32000.0, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500", 2, false, ""))
        foodList.add(Food("202", "Cheese Burger", "Burger bò kèm phô mai lát.", 38000.0, "https://images.unsplash.com/photo-1550547660-d9450f859349?w=500", 2, false, ""))

        // --- Mì Ý ---
        foodList.add(Food("501", "Jolly Spaghetti", "Mì Ý sốt bò bằm đặc trưng.", 40000.0, "https://images.unsplash.com/photo-1516100882582-96c3a05fe590?w=500", 5, false, "",
            listOf(FoodSize("Vừa", 0.0), FoodSize("Lớn", 15000.0))))

        // --- Cơm ---
        foodList.add(Food("701", "Cơm gà rán", "Cơm trắng kèm gà rán giòn.", 45000.0, "https://images.unsplash.com/photo-1562967914-608f82629710?w=500", 7, false, ""))
        foodList.add(Food("702", "Cơm burger steak", "Burger bò rưới sốt nấm và cơm.", 42000.0, "https://images.unsplash.com/photo-1594212699903-ec8a3eca50f5?w=500", 7, false, ""))

        // --- Ăn kèm ---
        foodList.add(Food("601", "Khoai tây chiên", "Khoai tây chiên vàng giòn.", 25000.0, "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=500", 6, false, "",
            listOf(FoodSize("Vừa", 0.0), FoodSize("Lớn", 10000.0))))

        // --- Đồ uống ---
        foodList.add(Food("301", "Nước ngọt", "Nước ngọt có gas mát lạnh.", 15000.0, "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=500", 3, false, "",
            listOf(FoodSize("Vừa", 0.0), FoodSize("Lớn", 5000.0))))
        
        foodList.add(Food("303", "Milo", "Sữa lúa mạch Milo thơm ngon.", 20000.0, "https://images.unsplash.com/photo-1541944743827-e04aa6427c33?w=500", 3, false, ""))

        // --- Tráng miệng ---
        foodList.add(Food("801", "Kem ly chocolate", "Kem socola mịn màng.", 15000.0, "https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=500", 8, false, ""))
        foodList.add(Food("804", "Peach Mango Pie", "Bánh pie đào xoài giòn rụm.", 28000.0, "https://images.unsplash.com/photo-1568571780765-9276ac8b75a2?w=500", 8, false, ""))
        foodList.add(Food("805", "Halo-halo", "Chè thập cẩm đặc trưng.", 45000.0, "https://images.unsplash.com/photo-1497034825429-c343d7c6a68f?w=500", 8, false, ""))
    }

    private fun setupRecyclerViews() {
        categoryAdapter = CategoryAdapter(categoryList) { category ->
            val filteredList = if (category.id == 0) foodList else foodList.filter { it.categoryId == category.id }
            foodAdapter.updateList(filteredList)
        }
        binding.recyclerViewCategory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewCategory.adapter = categoryAdapter

        foodAdapter = FoodAdapter(foodList) { food ->
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

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, Constants.SLIDER_DELAY)
    }
}