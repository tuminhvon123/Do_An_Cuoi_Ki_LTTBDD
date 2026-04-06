package com.example.appfood.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appfood.databinding.ActivityCartBinding
import com.example.appfood.presentation.adapter.CartAdapter
import com.example.appfood.presentation.viewmodel.CartViewModel
import com.example.appfood.util.PriceFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter()
        binding.recyclerViewCart.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(this@CartActivity)
        }

        cartAdapter.apply {
            onQuantityChanged = { cartItem, newQuantity ->
                cartViewModel.updateQuantity(cartItem, newQuantity)
            }

            onItemRemoved = { cartItem ->
                cartViewModel.removeFromCart(cartItem.id)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            cartViewModel.cartItems.collect { items ->
                cartAdapter.submitList(items)
                updateEmptyState(items.isEmpty())
            }
        }

        lifecycleScope.launch {
            cartViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            cartViewModel.errorMessage.collect { errorMessage ->
                errorMessage?.let {
                    // Show error message (you can use Toast, Snackbar, etc.)
                    cartViewModel.clearError()
                }
            }
        }

        lifecycleScope.launch {
            cartViewModel.cartSummary.collect { summary ->
                updateSummary(summary.first, summary.second)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBrowseMenu.setOnClickListener {
            // Navigate to main activity or menu
            finish()
        }

        binding.btnCheckout.setOnClickListener {
            if (cartViewModel.getTotalItems() > 0) {
                // Navigate to checkout activity
                startActivity(Intent(this, CheckoutActivity::class.java))
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            if (isEmpty) {
                layoutEmpty.visibility = View.VISIBLE
                recyclerViewCart.visibility = View.GONE
                layoutSummary.visibility = View.GONE
            } else {
                layoutEmpty.visibility = View.GONE
                recyclerViewCart.visibility = View.VISIBLE
                layoutSummary.visibility = View.VISIBLE
            }
        }
    }

    private fun updateSummary(totalItems: Int, totalPrice: Double) {
        binding.apply {
            tvTotalItems.text = "$totalItems món"
            tvTotalPrice.text = PriceFormatter.formatPrice(totalPrice)
            btnCheckout.isEnabled = totalItems > 0
        }
    }
}
