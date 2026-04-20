package com.example.appfood.presentation.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appfood.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var edtEmail: EditText
    private lateinit var btnSave: Button
    private lateinit var btnLogout: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // CHỐT CHẶN GUEST MODE: Kiểm tra ngay khi vừa mở trang
        if (auth.currentUser == null) {
            showGuestDialog()
            // Lệnh return để hệ thống dừng lại, không tải các nút bấm hay dữ liệu ở dưới nữa
            return
        }


        // PHẦN DƯỚI NÀY CHỈ CHẠY KHI ĐÃ ĐĂNG NHẬP

        edtName = findViewById(R.id.edtProfileName)
        edtPhone = findViewById(R.id.edtProfilePhone)
        edtAddress = findViewById(R.id.edtProfileAddress)
        edtEmail = findViewById(R.id.edtProfileEmail)
        btnSave = findViewById(R.id.btnSaveProfile)
        btnLogout = findViewById(R.id.btnLogout)

        loadUserProfile()

        btnSave.setOnClickListener { saveUserProfile() }
        btnLogout.setOnClickListener { logoutUser() }
    }

    // HÀM HIỂN THỊ BẢNG THÔNG BÁO CHO Guest
    private fun showGuestDialog() {
        AlertDialog.Builder(this)
            .setTitle("Yêu cầu đăng nhập")
            .setMessage("Bạn cần đăng nhập để xem và quản lý hồ sơ cá nhân. Bạn có muốn đăng nhập ngay bây giờ không?")
            .setCancelable(false) // Khóa màn hình, bắt buộc phải chọn 1 trong 2 nút
            .setPositiveButton("Đăng nhập") { _, _ ->
                // Mở trang Login
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // Đóng trang Profile lại
            }
            .setNegativeButton("Tiếp tục mua hàng") { _, _ ->
                // Đóng trang Profile, hệ thống sẽ tự động rơi về lại trang Chủ (MainActivity)
                finish()
            }
            .show()
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            edtEmail.setText(user.email)

            db.collection("Users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        edtName.setText(document.getString("fullName") ?: "")
                        edtPhone.setText(document.getString("phone") ?: "")
                        edtAddress.setText(document.getString("address") ?: "")
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            val name = edtName.text.toString().trim()
            val phone = edtPhone.text.toString().trim()
            val address = edtAddress.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng không để trống thông tin!", Toast.LENGTH_SHORT).show()
                return
            }

            val userMap = mapOf(
                "fullName" to name,
                "phone" to phone,
                "address" to address
            )

            db.collection("Users").document(user.uid)
                .update(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Lỗi cập nhật: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun logoutUser() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}