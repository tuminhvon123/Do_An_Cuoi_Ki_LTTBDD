package com.example.appfood.util

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.graphics.toColorInt
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat

object Extensions {
    
    fun Double.formatCurrency(): String {
        val formatter = DecimalFormat("#,### VNĐ")
        return formatter.format(this)
    }
    
    fun String.isValidEmail(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
    
    fun String.isStrongPassword(): Boolean {
        return this.length >= 6
    }

    /**
     * Hiển thị thông báo trong ứng dụng (In-App Notification)
     * @param isTop Nếu true, thông báo sẽ hiện từ trên xuống (giống FB, Youtube)
     */
    fun Activity.showNotification(message: String, isError: Boolean = false, isTop: Boolean = false) {
        val rootView = this.findViewById<View>(android.R.id.content) ?: return
        
        val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
        
        // Cấu hình màu sắc
        if (isError) {
            snackbar.setBackgroundTint("#E53935".toColorInt())
        } else {
            snackbar.setBackgroundTint("#4CAF50".toColorInt())
        }
        
        snackbar.setTextColor(Color.WHITE)
        
        // Xử lý vị trí nếu muốn hiện ở trên (Top)
        if (isTop) {
            val view = snackbar.view
            val params = view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            params.setMargins(0, 100, 0, 0) // Cách mép trên một chút
            view.layoutParams = params
            
            // Hiệu ứng animation từ trên xuống
            view.animation = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        }
        
        snackbar.show()
    }
}
