package com.example.appfood.presentation.ui.activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appfood.R
import com.example.appfood.databinding.ActivityCheckoutBinding
import com.example.appfood.presentation.adapter.OrderSummaryAdapter
import com.example.appfood.presentation.viewmodel.CheckoutViewModel
import com.example.appfood.util.PriceFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val checkoutViewModel: CheckoutViewModel by viewModels()
    private lateinit var orderAdapter: OrderSummaryAdapter

    private var selectedDeliveryType = "dine_in"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupDeliveryOptions()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderSummaryAdapter()
        binding.recyclerViewItems.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
        }
    }

    private fun setupDeliveryOptions() {
        // Setup click listeners for delivery type options
        binding.layoutDineIn.setOnClickListener {
            selectedDeliveryType = "dine_in"
            checkoutViewModel.setDeliveryType("dine_in")
            updateDeliveryTypeUI()
        }

        binding.layoutTakeaway.setOnClickListener {
            selectedDeliveryType = "takeaway"
            checkoutViewModel.setDeliveryType("takeaway")
            updateDeliveryTypeUI()
        }
    }

    private fun updateDeliveryTypeUI() {
        val isDineIn = selectedDeliveryType == "dine_in"

        binding.apply {
            if (isDineIn) {
                layoutDineIn.background = resources.getDrawable(R.drawable.bg_delivery_option_selected)
                layoutTakeaway.background = resources.getDrawable(R.drawable.bg_delivery_option)
            } else {
                layoutDineIn.background = resources.getDrawable(R.drawable.bg_delivery_option)
                layoutTakeaway.background = resources.getDrawable(R.drawable.bg_delivery_option_selected)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnConfirm.setOnClickListener {
            val customerName = binding.edtCustomerName.text.toString().trim()
            val customerPhone = binding.edtCustomerPhone.text.toString().trim()
            val specialNotes = binding.edtSpecialNotes.text.toString().trim()

            if (customerName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên khách hàng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (customerPhone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show final confirmation dialog
            showConfirmationDialog(customerName, customerPhone, specialNotes)
        }
    }

    private fun showConfirmationDialog(
        customerName: String,
        customerPhone: String,
        notes: String
    ) {
        val deliveryTypeText = if (selectedDeliveryType == "dine_in") "Tại bàn" else "Mang đi"
        val totalPrice = checkoutViewModel.totalPrice.value

        val message = """
            Xác nhận đơn hàng:
            
            Khách hàng: $customerName
            SĐT: $customerPhone
            Hình thức: $deliveryTypeText
            Tổng cộng: ${PriceFormatter.formatPrice(totalPrice)}
            
            Bạn có chắc chắn muốn đặt hàng không?
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Xác nhận đặt hàng")
            .setMessage(message)
            .setPositiveButton("Đồng ý") { dialog, _ ->
                dialog.dismiss()
                submitOrder(customerName, customerPhone, notes)
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun submitOrder(
        customerName: String,
        customerPhone: String,
        notes: String
    ) {
        // Use empty string for userId since the app doesn't have auth yet
        // In a real app, get the actual user ID from authentication
        val userId = "guest_user"
        checkoutViewModel.createOrder(userId, customerName, customerPhone, notes)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            checkoutViewModel.cartItems.collect { items ->
                orderAdapter.submitList(items)
            }
        }

        lifecycleScope.launch {
            checkoutViewModel.totalPrice.collect { price ->
                binding.tvTotalPrice.text = PriceFormatter.formatPrice(price)
            }
        }

        lifecycleScope.launch {
            checkoutViewModel.isLoading.collect { isLoading ->
                binding.btnConfirm.isEnabled = !isLoading
                binding.btnConfirm.text = if (isLoading) "Đang xử lý..." else "Xác nhận đặt hàng"
            }
        }

        lifecycleScope.launch {
            checkoutViewModel.errorMessage.collect { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(this@CheckoutActivity, it, Toast.LENGTH_SHORT).show()
                    checkoutViewModel.clearError()
                }
            }
        }

        lifecycleScope.launch {
            checkoutViewModel.orderSuccess.collect { success ->
                if (success) {
                    showSuccessDialog()
                }
            }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Thành công")
            .setMessage("Đơn hàng của bạn đã được tạo thành công!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                // Clear cart and return to main
                finish()
            }
            .setCancelable(false)
            .show()
    }
}

