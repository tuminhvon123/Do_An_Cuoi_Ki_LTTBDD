package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appfood.databinding.ActivityAddFoodBinding
import com.example.appfood.domain.model.Food
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFoodBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val categories = listOf("Chọn danh mục", "Pizza", "Burger", "Đồ uống", "Gà Rán", "Mì Ý", "Cơm", "Ăn kèm", "Tráng miệng")
    private val categoryIds = mapOf(
        "Pizza" to 1, "Burger" to 2, "Đồ uống" to 3, "Gà Rán" to 4, 
        "Mì Ý" to 5, "Cơm" to 7, "Ăn kèm" to 6, "Tráng miệng" to 8
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupListeners()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupListeners() {
        binding.toolbarAddFood.setNavigationOnClickListener { finish() }
        
        binding.btnAddFoodSubmit.setOnClickListener { validateAndSave() }
    }

    private fun validateAndSave() {
        val imageUrl = binding.edtImageUrl.text.toString().trim()
        val name = binding.edtFoodName.text.toString().trim()
        val priceStr = binding.edtFoodPrice.text.toString().trim()
        val desc = binding.edtFoodDesc.text.toString().trim()
        val categoryName = binding.spinnerCategory.selectedItem.toString()

        if (auth.currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để thực hiện quyền Admin!", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUrl.isEmpty() || name.isEmpty() || priceStr.isEmpty() || desc.isEmpty() || categoryName == "Chọn danh mục") {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
            return
        }

        saveFoodToFirestore(name, priceStr.toDouble(), desc, categoryIds[categoryName] ?: 0, imageUrl)
    }

    private fun saveFoodToFirestore(name: String, price: Double, desc: String, categoryId: Int, imageUrl: String) {
        binding.btnAddFoodSubmit.isEnabled = false
        
        val foodId = db.collection("Foods").document().id
        val food = Food(
            id = foodId,
            title = name,
            description = desc,
            price = price,
            imageUrl = imageUrl,
            categoryId = categoryId,
            isSoldOut = false
        )

        db.collection("Foods").document(foodId)
            .set(food)
            .addOnSuccessListener {
                Toast.makeText(this, "Thêm món ăn thành công!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnAddFoodSubmit.isEnabled = true
                Toast.makeText(this, "Lỗi lưu vào Database: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}