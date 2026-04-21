package com.example.appfood.presentation.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appfood.R
import com.example.appfood.util.Extensions.showNotification
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
    private lateinit var btnBack: ImageButton
    private lateinit var btnAdminPanel: Button
    private lateinit var tvUserEmail: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // CHỐT CHẶN GUEST MODE
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showGuestDialog()
            return
        }

        // Ánh xạ ID (Đã giải quyết xung đột)
        edtName = findViewById(R.id.edtProfileName)
        edtPhone = findViewById(R.id.edtProfilePhone)
        edtAddress = findViewById(R.id.edtProfileAddress)
        edtEmail = findViewById(R.id.edtProfileEmail)
        btnSave = findViewById(R.id.btnSaveProfile)
        btnLogout = findViewById(R.id.btnLogout)
        btnBack = findViewById(R.id.btnBack)
        btnAdminPanel = findViewById(R.id.btnAdminPanel)
        tvUserEmail = findViewById(R.id.tvUserEmail)

        val userEmail = currentUser.email ?: ""
        tvUserEmail.text = userEmail
        edtEmail.setText(userEmail)

        // Hiện nút đăng xuất khi đã đăng nhập
        btnLogout.visibility = View.VISIBLE

        // "Gán cứng" quyền Admin
        if (userEmail == "admin@gmail.com") {
            btnAdminPanel.visibility = View.VISIBLE
        } else {
            btnAdminPanel.visibility = View.GONE
        }

        loadUserProfile()

        btnAdminPanel.setOnClickListener {
            startActivity(Intent(this, AddFoodActivity::class.java))
        }

        btnSave.setOnClickListener { saveUserProfile() }

        btnBack.setOnClickListener {
            finish()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showGuestDialog() {
        AlertDialog.Builder(this)
            .setTitle("Yêu cầu đăng nhập")
            .setMessage("Bạn cần đăng nhập để xem và quản lý hồ sơ cá nhân. Bạn có muốn đăng nhập ngay bây giờ không?")
            .setCancelable(false)
            .setPositiveButton("Đăng nhập") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Tiếp tục mua hàng") { _, _ ->
                finish()
            }
            .show()
    }

    private fun loadUserProfile() {
        val user = auth.currentUser ?: return
        db.collection("Users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    edtName.setText(document.getString("fullName") ?: "")
                    edtPhone.setText(document.getString("phone") ?: "")
                    edtAddress.setText(document.getString("address") ?: "")
                }
            }
            .addOnFailureListener {
                showNotification("Không thể tải dữ liệu", isError = true, isTop = true)
            }
    }

    private fun saveUserProfile() {
        val user = auth.currentUser ?: return
        val name = edtName.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val address = edtAddress.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            showNotification("Vui lòng không để trống thông tin!", isError = true, isTop = true)
            return
        }

        val userMap = mapOf(
            "fullName" to name,
            "phone" to phone,
            "address" to address
        )

        db.collection("Users").document(user.uid)
            .set(userMap, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                showNotification("Cập nhật hồ sơ thành công!", isTop = true)
            }
            .addOnFailureListener { e ->
                showNotification("Lỗi cập nhật: ${e.message}", isError = true, isTop = true)
            }
    }
}