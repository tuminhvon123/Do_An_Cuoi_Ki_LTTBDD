package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import com.example.appfood.R

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    // 1. Khai báo các "Biến" đại diện cho giao diện
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoToRegister: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Giống như "địa chỉ" để kết nối giao diện từ activity_login
        setContentView(R.layout.activity_login)

        // Khởi tạo công cụ Firebase
        auth = FirebaseAuth.getInstance()

        // 2. Kết nối code với file giao diện
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)

        // 3. Xử lý sự kiện khi bấm nút Đăng nhập
        btnLogin.setOnClickListener {
            // Lấy chữ mà người dùng gõ vào ô nhập liệu
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            // Kiểm tra xem người dùng có bỏ trống ô nào không
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ Email và Mật khẩu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Dừng lại, không chạy tiếp xuống dưới
            }

            // Gọi Firebase để kiểm tra tài khoản
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Nếu đúng mật khẩu
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        // Chuyển sang MainActivity
                        val intent = android.content.Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Đóng LoginActivity
                    } else {
                        // Nếu sai mật khẩu hoặc chưa đăng ký
                        Toast.makeText(this, "Sai Email hoặc Mật khẩu!", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // 4. Lắng nghe sự kiện chuyển sang trang Đăng ký
        tvGoToRegister.setOnClickListener {
            // Chuyển từ trang LoginActivity sang RegisterActivity
            val intent = android.content.Intent(this, RegisterActivity::class.java)
            // Khởi hành!
            startActivity(intent)
        }
    }
}