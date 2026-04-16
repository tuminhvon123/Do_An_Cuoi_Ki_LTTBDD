package com.example.appfood.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfood.domain.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    init {
        loadOrders()
    }

    fun loadOrders() {
        firestore.collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING) // Sắp xếp ngay từ trên server
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val orderList = snapshot?.documents?.mapNotNull { doc ->
                    val order = doc.toObject(Order::class.java)
                    order?.id = doc.id
                    order
                } ?: emptyList()

                _orders.value = orderList
            }
    }

    fun updateRating(orderId: String, rating: Float, feedback: String, onSuccess: () -> Unit) {
        val updateData = mapOf("rating" to rating, "feedback" to feedback)
        firestore.collection("orders").document(orderId)
            .update(updateData)
            .addOnSuccessListener { onSuccess() }
    }
}
