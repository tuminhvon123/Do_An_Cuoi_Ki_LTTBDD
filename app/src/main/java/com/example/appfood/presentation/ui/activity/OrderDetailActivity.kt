package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.appfood.R
import com.example.appfood.databinding.ActivityOrderDetailBinding
import com.example.appfood.domain.model.CartItem
import com.example.appfood.domain.model.FoodRating
import com.example.appfood.domain.model.Order
import com.example.appfood.presentation.viewmodel.OrderDetailViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private val viewModel: OrderDetailViewModel by viewModels()
    private var currentOrder: Order? = null
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        binding.btnBack.setOnClickListener {
            finish()
        }

        val order = intent.getSerializableExtra("order_data") as? Order
        currentOrder = order

        order?.let {
            displayOrderInfo(it)
            // Load ratings for items in this order
            lifecycleScope.launch {
                viewModel.loadRatingsForOrder(it.items, currentUserId, it.id)
                displayOrderItems(it)
            }
        }
    }

    private fun displayOrderInfo(order: Order) {
        binding.txtOrderId.text = "Mã đơn: #${order.id.takeLast(5)}"
        binding.txtTotal.text = "Tổng tiền: ${String.format("%,.0f", order.totalPrice)}đ"

        val statusText = when (order.status.lowercase()) {
            "pending" -> "Đang chờ"
            "confirmed" -> "Đã xác nhận"
            "completed", "done" -> "Đã hoàn thành"
            "cancelled" -> "Đã hủy"
            else -> order.status
        }
        binding.txtStatus.text = "Trạng thái: $statusText"

        binding.txtCustomerName.text = "Tên: ${order.customerName}"
        binding.txtPhone.text = "SĐT: ${order.customerPhone}"
        if (order.isDineIn()) {
            binding.txtAddress.text =
                "Hình thức: Ăn tại bàn - Bàn số ${if (order.tableNumber.isNotEmpty()) order.tableNumber else "--"}"
        } else {
            binding.txtAddress.text =
                "Giao hàng: ${if (order.address.isNotEmpty()) order.address else "Chưa có địa chỉ"}"
        }
        binding.txtNote.text =
            if (order.notes.isNotEmpty()) "Ghi chú: ${order.notes}" else ""
    }

    private fun displayOrderItems(order: Order) {
        binding.itemsContainer.removeAllViews()

        val isCompleted = order.status.lowercase() in listOf("completed", "done")

        order.items.forEach { item ->
            val itemView = createOrderItemView(item, order.id, isCompleted)
            binding.itemsContainer.addView(itemView)
        }
    }

    private fun createOrderItemView(item: CartItem, orderId: String, isCompleted: Boolean): View {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_order_food_rating, null)

        val imgFood = itemView.findViewById<ImageView>(R.id.imgFood)
        val txtFoodName = itemView.findViewById<TextView>(R.id.txtFoodName)
        val txtFoodPrice = itemView.findViewById<TextView>(R.id.txtFoodPrice)
        val txtFoodQuantity = itemView.findViewById<TextView>(R.id.txtFoodQuantity)
        val ratingBar = itemView.findViewById<RatingBar>(R.id.itemRatingBar)
        val txtFeedback = itemView.findViewById<TextView>(R.id.txtItemFeedback)
        val btnRate = itemView.findViewById<Button>(R.id.btnRateItem)

        // Load food image
        Glide.with(this)
            .load(item.foodImageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(imgFood)

        txtFoodName.text = item.foodTitle
        txtFoodPrice.text = "${String.format("%,.0f", item.foodPrice)}đ"
        txtFoodQuantity.text = "x${item.quantity}"

        if (isCompleted) {
            if (item.isRated && item.rating > 0) {
                btnRate.visibility = View.GONE
                ratingBar.visibility = View.VISIBLE
                ratingBar.rating = item.rating

                if (item.feedback.isNotEmpty()) {
                    txtFeedback.visibility = View.VISIBLE
                    txtFeedback.text = "Nhận xét: ${item.feedback}"
                } else {
                    txtFeedback.visibility = View.GONE
                }
            } else {
                // Luôn hiện nút đánh giá, làm mờ nếu chưa đăng nhập
                btnRate.visibility = View.VISIBLE
                ratingBar.visibility = View.GONE
                txtFeedback.visibility = View.GONE
                
                if (currentUserId.isNotEmpty()) {
                    btnRate.isEnabled = true
                    btnRate.alpha = 1.0f
                    btnRate.setOnClickListener {
                        showRatingDialog(item, orderId)
                    }
                } else {
                    btnRate.isEnabled = false
                    btnRate.alpha = 0.5f
                    btnRate.setOnClickListener {
                        Toast.makeText(this@OrderDetailActivity, "Vui lòng đăng nhập để đánh giá!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            btnRate.visibility = View.GONE
            ratingBar.visibility = View.GONE
            txtFeedback.visibility = View.GONE
        }

        return itemView
    }

    private fun showRatingDialog(item: CartItem, orderId: String) {
        // Kiểm tra đăng nhập
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rating_food, null)
        val txtFoodTitle = dialogView.findViewById<TextView>(R.id.txtFoodTitle)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.dialogRatingBar)
        val edtFeedback = dialogView.findViewById<EditText>(R.id.edtFeedback)

        txtFoodTitle.text = item.foodTitle

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
            .apply {
                dialogView.findViewById<Button>(R.id.btnSubmitRating).setOnClickListener {
                    val ratingValue = ratingBar.rating
                    val feedbackText = edtFeedback.text.toString().trim()

                    if (ratingValue == 0f) {
                        Toast.makeText(context, "Vui lòng chọn số sao!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val foodRating = FoodRating(
                        foodId = item.foodId,
                        foodTitle = item.foodTitle,
                        orderId = orderId,
                        userId = currentUserId,
                        userEmail = currentUserEmail,
                        rating = ratingValue,
                        feedback = feedbackText
                    )

                    lifecycleScope.launch {
                        viewModel.addFoodRating(foodRating, item)
                        Toast.makeText(context, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show()
                        dismiss()

                        // Refresh the display
                        currentOrder?.let { order ->
                            item.rating = ratingValue
                            item.feedback = feedbackText
                            item.isRated = true
                            displayOrderItems(order)
                        }
                    }
                }
                show()
            }
    }
}