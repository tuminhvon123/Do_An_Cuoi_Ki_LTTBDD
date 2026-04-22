package com.example.appfood.data.repository

import com.example.appfood.domain.model.Order
import com.example.appfood.domain.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : OrderRepository {

    override suspend fun createOrder(order: Order): Result<String> {
        return try {
            val documentRef = firestore.collection("orders").document()
            // Gán ID của document vừa tạo vào object order
            val orderWithId = order.copy(id = documentRef.id)
            documentRef.set(orderWithId).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val snapshot = firestore.collection("orders").document(orderId).get().await()
            val order = snapshot.toObject(Order::class.java)
            if (order != null) Result.success(order)
            else Result.failure(Exception("Order not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserOrders(userId: String): Flow<List<Order>> = callbackFlow {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val isAdmin = currentUser?.email == "admin@gmail.com"

        val db = FirebaseFirestore.getInstance()

        val query = if (isAdmin) {
            db.collection("orders")
        } else {
            db.collection("orders").whereEqualTo("userId", userId) // user thi loc them id
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val orders = snapshot?.documents?.mapNotNull { doc ->
                val order = doc.toObject(Order::class.java)
                order?.apply { id = doc.id }
            } ?: emptyList()

            trySend(orders)
        }

        awaitClose { listener.remove() }
    }

    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            firestore.collection("orders").document(orderId)
                .update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderRating(orderId: String, rating: Float, feedback: String): Result<Unit> {
        return try {
            firestore.collection("orders").document(orderId)
                .update(mapOf("rating" to rating, "feedback" to feedback)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}