package com.example.appfood.presentation.ui.activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val checkoutViewModel: CheckoutViewModel by viewModels()
    private lateinit var orderAdapter: OrderSummaryAdapter
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
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
        autoFillUserData()
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
        binding.layoutDineIn.setOnClickListener {
            selectedDeliveryType = "dine_in"
            checkoutViewModel.setDeliveryType("dine_in")
            updateDeliveryTypeUI()

            // Hiện ô Bàn số, Ẩn ô Địa chỉ
            binding.edtCustomerTable.visibility = View.VISIBLE
            binding.edtCustomerAddress.visibility = View.GONE
        }

        binding.layoutTakeaway.setOnClickListener {
            selectedDeliveryType = "takeaway"
            checkoutViewModel.setDeliveryType("takeaway")
            updateDeliveryTypeUI()

            // Ẩn ô Bàn số, Hiện ô Địa chỉ
            binding.edtCustomerTable.visibility = View.GONE
            binding.edtCustomerAddress.visibility = View.VISIBLE
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
        val isDineIn = selectedDeliveryType == "dine_in"
        val deliveryTypeText = if (isDineIn) "Tại bàn" else "Mang đi"
        val totalPrice = checkoutViewModel.totalPrice.value

        // Lấy thêm thông tin Bàn hoặc Địa chỉ từ giao diện
        val extraInfo = if (isDineIn) {
            "Bàn số: ${binding.edtCustomerTable.text.toString().trim()}"
        } else {
            "Địa chỉ: ${binding.edtCustomerAddress.text.toString().trim()}"
        }

        // Chèn biến extraInfo vào giữa thông báo
        val message = """
            Xác nhận đơn hàng:
            
            Khách hàng: $customerName
            SĐT: $customerPhone
            Hình thức: $deliveryTypeText
            $extraInfo
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
        val currentUser = auth.currentUser

        // Nếu có đăng nhập thì lấy ID thật, nếu không thì tạo guest ID và lưu lại
        val userId = currentUser?.uid ?: generateAndSaveGuestId()

        // Tiến hành tạo đơn hàng bình thường
        checkoutViewModel.createOrder(userId, customerName, customerPhone, notes, binding.edtCustomerTable.text.toString(),
            binding.edtCustomerAddress.text.toString())
    }

    private fun generateAndSaveGuestId(): String {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        var guestId = prefs.getString("guest_user_id", null)
        if (guestId == null) {
            guestId = "guest_${System.currentTimeMillis()}"
            prefs.edit().putString("guest_user_id", guestId).apply()
        }
        return guestId
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

                val intent = Intent(this, OrderHistoryActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)

                finish()
            }
            .setCancelable(false)
            .show()
    }
    private fun autoFillUserData() {
        val currentUser = auth.currentUser

        // Chỉ đi kéo dữ liệu nếu khách ĐÃ đăng nhập
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()

            db.collection("Users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName") ?: ""
                        val phone = document.getString("phone") ?: ""
                        val address = document.getString("address") ?: ""

                        // Tự động điền chữ vào các ô
                        binding.edtCustomerName.setText(fullName)
                        binding.edtCustomerPhone.setText(phone)
                        binding.edtCustomerAddress.setText(address)
                    }
                }
        }
    }
}

