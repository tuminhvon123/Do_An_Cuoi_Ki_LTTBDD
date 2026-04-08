package com.example.appfood.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appfood.R
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val auth = FirebaseAuth.getInstance()

        // 1. Lấy thông tin tài khoản đang đăng nhập hiện lên màn hình
        val currentUser = auth.currentUser
        if (currentUser != null) {
            tvUserEmail.text = currentUser.email
        }

        // 2. Lắng nghe nút Đăng xuất
        btnLogout.setOnClickListener {
            // Xóa đăng nhập trên điện thoại
            auth.signOut()

            // Chuyển về màn hình Login
            val intent = Intent(this, LoginActivity::class.java)
            // Lệnh đặc biệt: Xóa lịch sử trang để khách không bấm Back vào lại app được
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}