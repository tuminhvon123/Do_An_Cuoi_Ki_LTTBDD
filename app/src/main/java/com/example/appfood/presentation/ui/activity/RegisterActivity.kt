package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appfood.R
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var edtRegEmail: EditText
    private lateinit var edtRegPassword: EditText
    private lateinit var edtRegConfirmPassword: EditText
    private lateinit var edtRegFullName: EditText
    private lateinit var edtRegPhone: EditText
    private lateinit var edtRegAddress: EditText
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
        edtRegFullName = findViewById(R.id.edtFullNameRegister)
        edtRegPhone = findViewById(R.id.edtPhoneRegister)
        edtRegAddress = findViewById(R.id.edtAddressRegister)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoBackLogin = findViewById(R.id.tvGoBackLogin)

        // Xử lý khi bấm nút Đăng Ký
        btnRegister.setOnClickListener {
            val email = edtRegEmail.text.toString().trim()
            val password = edtRegPassword.text.toString().trim()
            val confirmPassword = edtRegConfirmPassword.text.toString().trim()
            val fullName = edtRegFullName.text.toString().trim()
            val phone = edtRegPhone.text.toString().trim()
            val address = edtRegAddress.text.toString().trim()

            // 1. Kiểm tra không được để trống
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                fullName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ tất cả các ô!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Kiểm tra mật khẩu
            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Tiến hành tạo tài khoản trên Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 4. Nếu tạo tài khoản thành công, lấy ID và tiến hành cất dữ liệu vào Firestore
                        val userId = auth.currentUser?.uid

                        if (userId != null) {
                            // Tạo gói hàng (Map) chứa dữ liệu
                            val userProfileMap = hashMapOf(
                                "fullName" to fullName,
                                "phone" to phone,
                                "address" to address,
                                "email" to email
                            )

                            // Mở Két sắt Firestore và nạp dữ liệu
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            db.collection("Users").document(userId)
                                .set(userProfileMap)
                                .addOnSuccessListener {
                                    // Thành công CẢ HAI BƯỚC (Auth + Firestore)
                                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                    finish() // Về trang Login
                                }
                                .addOnFailureListener { e ->
                                    // Lỗi khi lưu Firestore
                                    Toast.makeText(this, "Lỗi lưu thông tin: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        // Lỗi khi tạo tài khoản (vd: email trùng, pass ngắn)
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