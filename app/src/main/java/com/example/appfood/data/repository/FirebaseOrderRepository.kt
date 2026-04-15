package com.example.appfood.data.repository

import android.util.Log
import com.example.appfood.domain.model.Order
import com.example.appfood.domain.repository.OrderRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseOrderRepository @Inject constructor() : OrderRepository {

    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.getReference("orders")
    private val TAG = "FirebaseOrderRepository"

    override suspend fun createOrder(order: Order): Result<String> {
        return try {
            Log.d(TAG, "Creating order for: ${order.customerName}")

            val orderId = ordersRef.push().key ?: throw Exception("Failed to generate order ID")
            val orderWithId = order.copy(id = orderId)

            Log.d(TAG, "Order ID: $orderId")
            Log.d(TAG, "Order data: $orderWithId")

            // Push order to Firebase
            ordersRef.child(orderId).setValue(orderWithId).await()

            Log.d(TAG, "Order created successfully: $orderId")
            Result.success(orderId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating order: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val snapshot = ordersRef.child(orderId).get().await()
            val order = snapshot.getValue(Order::class.java)
            if (order != null) {
                Result.success(order)
            } else {
                Result.failure(Exception("Order not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting order: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserOrders(userId: String): Flow<List<Order>> {
        val ordersFlow = MutableStateFlow<List<Order>>(emptyList())

        try {
            ordersRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val orders = mutableListOf<Order>()
                        for (child in snapshot.children) {
                            val order = child.getValue(Order::class.java)
                            if (order != null) {
                                orders.add(order)
                            }
                        }
                        ordersFlow.value = orders
                        Log.d(TAG, "Got ${orders.size} orders for user $userId")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error getting user orders: ${error.message}")
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up listener: ${e.message}", e)
        }

        return ordersFlow
    }

    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            ordersRef.child(orderId).child("status").setValue(status).await()
            Log.d(TAG, "Order status updated: $orderId -> $status")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order status: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateOrderRating(orderId: String, rating: Float, feedback: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "rating" to rating,
                "feedback" to feedback
            )
            ordersRef.child(orderId).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
