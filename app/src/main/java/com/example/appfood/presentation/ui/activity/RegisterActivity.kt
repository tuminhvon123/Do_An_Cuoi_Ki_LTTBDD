package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appfood.R
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var edtRegEmail: EditText
    private lateinit var edtRegPassword: EditText
    private lateinit var edtRegConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoBackLogin: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // Ánh xạ ID
        edtRegEmail = findViewById(R.id.edtRegEmail)
        edtRegPassword = findViewById(R.id.edtRegPassword)
        edtRegConfirmPassword = findViewById(R.id.edtRegConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoBackLogin = findViewById(R.id.tvGoBackLogin)

        // Xử lý khi bấm nút Đăng Ký
        btnRegister.setOnClickListener {
            val email = edtRegEmail.text.toString().trim()
            val password = edtRegPassword.text.toString().trim()
            val confirmPassword = edtRegConfirmPassword.text.toString().trim()

            // 1. Kiểm tra không được để trống
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Kiểm tra mật khẩu nhập lại có khớp không
            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Đẩy thông tin lên Firebase để tạo tài khoản
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                        // Tự động đóng màn hình Đăng ký, quay về màn hình Đăng nhập
                        finish()
                    } else {
                        Toast.makeText(this, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Nút quay lại Đăng nhập
        tvGoBackLogin.setOnClickListener {
            finish() // Lệnh finish() giúp đóng màn hình hiện tại lại
        }
    }
}