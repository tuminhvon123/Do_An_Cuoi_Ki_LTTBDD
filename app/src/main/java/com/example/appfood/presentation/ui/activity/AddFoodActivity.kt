package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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
    
    private var editFood: Food? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Kiểm tra xem là chế độ Sửa hay Thêm mới
        editFood = intent.getSerializableExtra("food") as? Food

        setupSpinner()
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        if (editFood != null) {
            binding.toolbarAddFood.title = "Sửa món ăn"
            binding.btnAddFoodSubmit.text = "CẬP NHẬT MÓN ĂN"
            binding.btnDeleteFood.visibility = View.VISIBLE
            
            // Đổ dữ liệu cũ vào view
            editFood?.let { food ->
                binding.edtImageUrl.setText(food.imageUrl)
                binding.edtFoodName.setText(food.title)
                binding.edtFoodPrice.setText(food.price.toLong().toString())
                binding.edtFoodDesc.setText(food.description)
                binding.cbSoldOut.isChecked = food.isSoldOut
                
                // Chọn lại category đúng
                val categoryName = categoryIds.filterValues { it == food.categoryId }.keys.firstOrNull()
                val index = categories.indexOf(categoryName)
                if (index != -1) binding.spinnerCategory.setSelection(index)

                Glide.with(this).load(food.imageUrl).into(binding.imgPreview)
            }
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupListeners() {
        binding.toolbarAddFood.setNavigationOnClickListener { finish() }
        
        binding.btnAddFoodSubmit.setOnClickListener { validateAndSave() }

        binding.btnDeleteFood.setOnClickListener {
            showDeleteConfirmDialog()
        }
        
        // Preview ảnh khi nhập URL
        binding.edtImageUrl.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val url = binding.edtImageUrl.text.toString()
                if (url.isNotEmpty()) {
                    Glide.with(this).load(url).into(binding.imgPreview)
                }
            }
        }
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Xóa món ăn")
            .setMessage("Bạn có chắc chắn muốn xóa món này không? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa") { _, _ -> deleteFood() }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteFood() {
        val id = editFood?.id ?: return
        binding.btnDeleteFood.isEnabled = false
        
        db.collection("Foods").document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Đã xóa món ăn!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnDeleteFood.isEnabled = true
                Toast.makeText(this, "Lỗi khi xóa: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateAndSave() {
        val imageUrl = binding.edtImageUrl.text.toString().trim()
        val name = binding.edtFoodName.text.toString().trim()
        val priceStr = binding.edtFoodPrice.text.toString().trim()
        val desc = binding.edtFoodDesc.text.toString().trim()
        val categoryName = binding.spinnerCategory.selectedItem.toString()
        val isSoldOut = binding.cbSoldOut.isChecked

        if (auth.currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để thực hiện quyền Admin!", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUrl.isEmpty() || name.isEmpty() || priceStr.isEmpty() || desc.isEmpty() || categoryName == "Chọn danh mục") {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
            return
        }

        saveFoodToFirestore(name, priceStr.toDouble(), desc, categoryIds[categoryName] ?: 0, imageUrl, isSoldOut)
    }

    private fun saveFoodToFirestore(name: String, price: Double, desc: String, categoryId: Int, imageUrl: String, isSoldOut: Boolean) {
        binding.btnAddFoodSubmit.isEnabled = false
        
        val foodId = editFood?.id ?: db.collection("Foods").document().id
        val food = Food(
            id = foodId,
            title = name,
            description = desc,
            price = price,
            imageUrl = imageUrl,
            categoryId = categoryId,
            isSoldOut = isSoldOut
        )

        db.collection("Foods").document(foodId)
            .set(food)
            .addOnSuccessListener {
                val msg = if (editFood != null) "Cập nhật thành công!" else "Thêm món ăn thành công!"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnAddFoodSubmit.isEnabled = true
                Toast.makeText(this, "Lỗi lưu Database: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}