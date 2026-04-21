package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.appfood.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var edtRegEmail: EditText
    private lateinit var edtRegPassword: EditText
    private lateinit var edtRegConfirmPassword: EditText
    private lateinit var edtRegFullName: EditText
    private lateinit var edtRegPhone: EditText
    private lateinit var edtRegAddress: EditText
    private lateinit var edtRegProfileLink: EditText
    private lateinit var imgRegPreview: ImageView
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
        edtRegProfileLink = findViewById(R.id.edtRegProfileLink)
        imgRegPreview = findViewById(R.id.imgRegPreview)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoBackLogin = findViewById(R.id.tvGoBackLogin)

        // Tính năng xem trước ảnh đại diện khi dán link
        edtRegProfileLink.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val url = s.toString().trim()
                if (url.isNotEmpty()) {
                    Glide.with(this@RegisterActivity)
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_myplaces)
                        .error(android.R.drawable.ic_menu_myplaces)
                        .circleCrop()
                        .into(imgRegPreview)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Xử lý khi bấm nút Đăng Ký
        btnRegister.setOnClickListener {
            val email = edtRegEmail.text.toString().trim()
            val password = edtRegPassword.text.toString().trim()
            val confirmPassword = edtRegConfirmPassword.text.toString().trim()
            val fullName = edtRegFullName.text.toString().trim()
            val phone = edtRegPhone.text.toString().trim()
            val address = edtRegAddress.text.toString().trim()
            val profileImage = edtRegProfileLink.text.toString().trim()

            // Kiểm tra trống
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                fullName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ tất cả các ô!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra mật khẩu
            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tiến hành tạo tài khoản trên Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            // Lưu thêm link ảnh vào Firestore
                            val userProfileMap = hashMapOf(
                                "fullName" to fullName,
                                "phone" to phone,
                                "address" to address,
                                "email" to email,
                                "profileImage" to profileImage
                            )

                            val db = FirebaseFirestore.getInstance()
                            db.collection("Users").document(userId)
                                .set(userProfileMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Lỗi lưu thông tin: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        tvGoBackLogin.setOnClickListener { finish() }
    }
}