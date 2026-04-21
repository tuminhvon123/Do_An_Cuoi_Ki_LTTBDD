package com.example.appfood.presentation.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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
    private lateinit var edtProfileImageLink: EditText
    private lateinit var imgProfile: ImageView
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

        val currentUser = auth.currentUser
        if (currentUser == null) {
            showGuestDialog()
            return
        }

        // Ánh xạ ID
        edtName = findViewById(R.id.edtProfileName)
        edtPhone = findViewById(R.id.edtProfilePhone)
        edtAddress = findViewById(R.id.edtProfileAddress)
        edtEmail = findViewById(R.id.edtProfileEmail)
        edtProfileImageLink = findViewById(R.id.edtProfileImageLink)
        imgProfile = findViewById(R.id.imgProfile)
        btnSave = findViewById(R.id.btnSaveProfile)
        btnLogout = findViewById(R.id.btnLogout)
        btnBack = findViewById(R.id.btnBack)
        btnAdminPanel = findViewById(R.id.btnAdminPanel)
        tvUserEmail = findViewById(R.id.tvUserEmail)

        val userEmail = currentUser.email ?: ""
        tvUserEmail.text = userEmail
        edtEmail.setText(userEmail)

        if (userEmail == "admin@gmail.com") {
            btnAdminPanel.visibility = View.VISIBLE
        }

        loadUserProfile()
        setupListeners()
    }

    private fun setupListeners() {
        // Preview ảnh khi dán link
        edtProfileImageLink.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val url = s.toString().trim()
                if (url.isNotEmpty()) {
                    Glide.with(this@ProfileActivity)
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_myplaces)
                        .error(android.R.drawable.ic_menu_myplaces)
                        .circleCrop()
                        .into(imgProfile)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnAdminPanel.setOnClickListener {
            startActivity(Intent(this, AddFoodActivity::class.java))
        }

        btnSave.setOnClickListener { saveUserProfile() }

        btnBack.setOnClickListener { finish() }

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
            .setMessage("Bạn cần đăng nhập để xem và quản lý hồ sơ cá nhân.")
            .setCancelable(false)
            .setPositiveButton("Đăng nhập") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Quay lại") { _, _ -> finish() }
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
                    val imageUrl = document.getString("profileImage") ?: ""
                    edtProfileImageLink.setText(imageUrl)
                    
                    if (imageUrl.isNotEmpty()) {
                        Glide.with(this).load(imageUrl).circleCrop().into(imgProfile)
                    }
                }
            }
    }

    private fun saveUserProfile() {
        val user = auth.currentUser ?: return
        val name = edtName.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val address = edtAddress.text.toString().trim()
        val profileImage = edtProfileImageLink.text.toString().trim()

        val userMap = mapOf(
            "fullName" to name,
            "phone" to phone,
            "address" to address,
            "profileImage" to profileImage
        )

        db.collection("Users").document(user.uid)
            .set(userMap, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}