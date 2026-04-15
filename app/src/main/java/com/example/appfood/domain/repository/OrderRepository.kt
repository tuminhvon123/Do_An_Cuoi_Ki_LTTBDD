package com.example.appfood.domain.repository

import com.example.appfood.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun createOrder(order: Order): Result<String>
    suspend fun getOrderById(orderId: String): Result<Order>
    suspend fun getUserOrders(userId: String): Flow<List<Order>>
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit>
    suspend fun updateOrderRating(orderId: String, rating: Float, feedback: String): Result<Unit>
}
