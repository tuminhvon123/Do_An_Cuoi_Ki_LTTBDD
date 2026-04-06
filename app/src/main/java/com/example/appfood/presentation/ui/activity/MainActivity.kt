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
import com.example.appfood.presentation.viewmodel.MainViewModel
import com.example.appfood.presentation.viewmodel.CartViewModel
import com.example.appfood.util.Constants
import com.example.appfood.util.PriceFormatter
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
        var currentItem = binding.viewPagerBanner.currentItem
        currentItem++
        if (currentItem >= bannerAdapter.itemCount) {
            currentItem = 0
        }
        binding.viewPagerBanner.setCurrentItem(currentItem, true)
    }

    private val mainViewModel: MainViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            setupToolbar()
            initData() 
            setupBanner()
            setupRecyclerViews()
            setupSearch()
            setupCart()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
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
        binding.viewPagerBanner.visibility = View.VISIBLE
        bannerAdapter = BannerAdapter(bannerImages)
        binding.viewPagerBanner.adapter = bannerAdapter
        
        // Tự động chạy slider sau mỗi 3 giây
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
        categoryList.add(Category(0, "Tất cả", "https://cdn-icons-png.flaticon.com/512/1046/1046771.png"))
        categoryList.add(Category(1, "Pizza", "https://cdn-icons-png.flaticon.com/512/3595/3595455.png"))
        categoryList.add(Category(2, "Burger", "https://cdn-icons-png.flaticon.com/512/706/706918.png"))
        categoryList.add(Category(3, "Drink", "https://cdn-icons-png.flaticon.com/512/2405/2405479.png"))
        categoryList.add(Category(4, "Chicken", "https://cdn-icons-png.flaticon.com/512/3143/3143640.png"))

        foodList.clear()
        foodList.add(Food("1", "Pizza Hải Sản", "Pizza hải sản cao cấp với tôm và mực tươi.", 150000.0, "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500&auto=format", 1, false, "Thêm phô mai, Đế dày"))
        foodList.add(Food("2", "Burger Bò", "Burger bò Mỹ với phô mai tan chảy.", 55000.0, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format", 2, false, "Thêm thịt, Ít rau"))
        foodList.add(Food("3", "Coca Cola", "Nước giải khát Coca Cola mát lạnh.", 15000.0, "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=500&auto=format", 3, true, ""))
        foodList.add(Food("4", "Gà Rán", "Gà rán giòn tan chuẩn vị KFC.", 35000.0, "https://images.unsplash.com/photo-1562967914-608f82629710?w=500&auto=format", 4, false, "Thêm tương ớt"))
        foodList.add(Food("5", "Pizza Phô Mai", "Nhiều phô mai kéo sợi cực hấp dẫn.", 120000.0, "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&auto=format", 1, false, ""))
    }

    private fun setupRecyclerViews() {
        categoryAdapter = CategoryAdapter(categoryList) { category ->
            val filteredList = if (category.name == "Tất cả") foodList else foodList.filter { it.categoryId == category.id }
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