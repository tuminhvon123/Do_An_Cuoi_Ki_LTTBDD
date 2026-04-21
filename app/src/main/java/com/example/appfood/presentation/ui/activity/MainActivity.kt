package com.example.appfood.presentation.ui.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.appfood.presentation.ui.adapter.BannerAdapter
import com.example.appfood.presentation.ui.adapter.CategoryAdapter
import com.example.appfood.presentation.ui.adapter.FoodAdapter
import com.example.appfood.presentation.adapter.SearchHistoryAdapter
import com.example.appfood.databinding.ActivityMainBinding
import com.example.appfood.domain.model.Category
import com.example.appfood.domain.model.Food
import com.example.appfood.presentation.viewmodel.MainViewModel
import com.example.appfood.presentation.viewmodel.CartViewModel
import com.example.appfood.util.Constants
import com.example.appfood.util.Extensions.showNotification
import com.example.appfood.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private lateinit var sharedPreferences: SharedPreferences

    private var foodList = mutableListOf<Food>()
    private var categoryList = mutableListOf<Category>()
    private var searchHistoryList = mutableListOf<String>()
    private val db = FirebaseFirestore.getInstance()
    private var dontShowProfileReminder = false 

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

        sharedPreferences = getSharedPreferences("search_history", Context.MODE_PRIVATE)
        loadSearchHistory()

        initCategories() 
        setupBanner()
        setupRecyclerViews()
        setupSearch()
        setupCart()
        setupBottomNav()
        setupAdminFab()
        
        observeFoodsFromFirestore()
        checkUserProfile()
    }

    // --- PHẦN LỊCH SỬ TÌM KIẾM (SEARCH HISTORY) ---

    // 1. Tải lịch sử từ bộ nhớ máy (SharedPreferences) lên danh sách hiển thị
    private fun loadSearchHistory() {
        val json = sharedPreferences.getString("history", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            searchHistoryList = Gson().fromJson(json, type)
        }
    }

    // 2. Lưu từ khóa mới khi người dùng nhấn Enter/Search
    private fun saveSearchQuery(query: String) {
        if (query.isEmpty()) return
        
        // Xóa từ cũ nếu bị trùng để đưa từ mới lên vị trí đầu tiên
        searchHistoryList.remove(query)
        searchHistoryList.add(0, query)
        
        // Giới hạn tối đa 10 mục lịch sử gần nhất
        if (searchHistoryList.size > 10) {
            searchHistoryList.removeAt(searchHistoryList.size - 1)
        }
        
        // Chuyển danh sách sang định dạng JSON để lưu vào máy
        val json = Gson().toJson(searchHistoryList)
        sharedPreferences.edit().putString("history", json).apply()
        updateSearchHistoryUI()
    }

    // 3. Xóa một mục cụ thể trong lịch sử (được gọi khi nhấn giữ/long click)
    private fun deleteSearchQuery(query: String) {
        searchHistoryList.remove(query)
        val json = Gson().toJson(searchHistoryList)
        sharedPreferences.edit().putString("history", json).apply()
        updateSearchHistoryUI()
    }

    private fun setupSearch() {
        // Cấu hình danh sách Dropdown cho lịch sử tìm kiếm
        searchHistoryAdapter = SearchHistoryAdapter(
            searchHistoryList,
            onItemClick = { query ->
                // Khi nhấn vào một mục: Điền chữ vào ô và ẩn Dropdown
                binding.searchEditText.setText(query)
                binding.searchEditText.setSelection(query.length)
                binding.cardSearchHistory.visibility = View.GONE
            },
            onDeleteClick = { query ->
                // Khi nhấn xóa: Cập nhật lại danh sách hoặc ẩn nếu hết sạch
                deleteSearchQuery(query)
                if (searchHistoryList.isEmpty()) {
                    binding.cardSearchHistory.visibility = View.GONE
                }
            }
        )
        binding.rvSearchHistoryDropdown.layoutManager = LinearLayoutManager(this)
        binding.rvSearchHistoryDropdown.adapter = searchHistoryAdapter

        // Lắng nghe sự kiện nhấn vào ô tìm kiếm để HIỆN DROPDOWN
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchHistoryList.isNotEmpty() && binding.searchEditText.text.isEmpty()) {
                binding.cardSearchHistory.visibility = View.VISIBLE
            } else {
                binding.cardSearchHistory.visibility = View.GONE
            }
        }

        // Lắng nghe sự kiện nhấn ENTER (Nút kính lúp) trên bàn phím để LƯU LỊCH SỬ
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    saveSearchQuery(query)
                }
                binding.cardSearchHistory.visibility = View.GONE
            }
            false
        }

        // Lắng nghe thay đổi chữ khi gõ để lọc món ăn và ẨN DROPDOWN khi đang gõ
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                binding.cardSearchHistory.visibility = View.GONE
                
                // Lọc danh sách món ăn theo tên
                val filteredList = foodList.filter { it.title.contains(query, ignoreCase = true) }
                foodAdapter.updateList(filteredList)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateSearchHistoryUI() {
        if (::searchHistoryAdapter.isInitialized) {
            searchHistoryAdapter.updateList(searchHistoryList)
        }
    }

    private fun checkUserProfile() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser ?: return

        if (dontShowProfileReminder) return

        db.collection("Users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val fullName = document.getString("fullName") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val address = document.getString("address") ?: ""

                    if (fullName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                        showProfileReminderDialog()
                    }
                } else {
                    showProfileReminderDialog()
                }
            }
    }

    private fun showProfileReminderDialog() {
        val dialogView = View.inflate(this, R.layout.dialog_profile_reminder, null)
        val checkBoxDontShow = dialogView.findViewById<CheckBox>(R.id.checkBoxDontShow)

        AlertDialog.Builder(this)
            .setTitle("Cập nhật thông tin cá nhân")
            .setMessage("Để mua hàng thuận tiện hơn, vui lòng cập nhật đầy đủ thông tin tên, số điện thoại và địa chỉ giao hàng.")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Cập nhật profile") { _, _ ->
                if (checkBoxDontShow.isChecked) dontShowProfileReminder = true
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            .setNegativeButton("Để sau") { _, _ ->
                if (checkBoxDontShow.isChecked) dontShowProfileReminder = true
            }
            .show()
    }

    private fun setupAdminFab() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.email == "admin@gmail.com") {
            binding.fabAddFoodAdmin.visibility = View.VISIBLE
            binding.fabAddFoodAdmin.setOnClickListener {
                // assume AddFoodActivity exists based on context
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
        db.collection("Foods").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
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
                R.id.nav_home -> {
                    binding.cardSearchHistory.visibility = View.GONE
                    true
                }
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
        setupAdminFab()
        sliderHandler.postDelayed(sliderRunnable, Constants.SLIDER_DELAY)
    }
}