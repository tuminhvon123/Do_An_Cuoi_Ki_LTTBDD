package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appfood.databinding.ActivityOrderDetailBinding
import com.example.appfood.domain.model.Order
class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener {
            finish()
        }
        val order = intent.getSerializableExtra("order_data") as? Order

        order?.let {
            binding.txtOrderId.text = "Mã đơn: #${it.id.takeLast(5)}"
            binding.txtTotal.text = "Tổng tiền: ${String.format("%,.0f", it.totalPrice)}đ"
            binding.txtStatus.text = it.status
            binding.txtCustomerName.text = "Tên: ${it.customerName}"
            binding.txtPhone.text = "SĐT: ${it.customerPhone}"
            if (it.isDineIn()) {
                binding.txtAddress.text =
                    "Hình thức: Ăn tại bàn - Bàn số ${if (it.tableNumber.isNotEmpty()) it.tableNumber else "--"}"
            } else {
                binding.txtAddress.text =
                    "Giao hàng: ${if (it.address.isNotEmpty()) it.address else "Chưa có địa chỉ"}"
            }
            binding.txtNote.text =
                if (it.notes.isNotEmpty()) "Ghi chú: ${it.notes}" else ""
            binding.txtItems.text = it.items.joinToString("\n") { item ->
                val priceFormatted = String.format("%,.0f", item.foodPrice)
                val totalItemPrice = item.foodPrice * item.quantity

                "${item.foodTitle} - ${priceFormatted}đ x ${item.quantity} (${String.format("%,.0f", totalItemPrice)}đ)"
            }
        }
    }
}