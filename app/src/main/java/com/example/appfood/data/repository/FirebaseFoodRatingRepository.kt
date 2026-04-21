package com.example.appfood.data.repository

import android.util.Log
import com.example.appfood.domain.model.FoodRating
import com.example.appfood.domain.repository.FoodRatingRepository
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
class FirebaseFoodRatingRepository @Inject constructor() : FoodRatingRepository {

    private val database = FirebaseDatabase.getInstance()
    private val ratingsRef = database.getReference("food_ratings")
    private val TAG = "FoodRatingRepository"

    override suspend fun addRating(rating: FoodRating): Result<String> {
        return try {
            val ratingId = ratingsRef.push().key ?: throw Exception("Failed to generate rating ID")
            val ratingWithId = rating.copy(id = ratingId)
            
            ratingsRef.child(ratingId).setValue(ratingWithId).await()
            Log.d(TAG, "Rating added successfully: $ratingId for food ${rating.foodId}")
            Result.success(ratingId)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding rating: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getRatingsByFoodId(foodId: String): Flow<List<FoodRating>> {
        val ratingsFlow = MutableStateFlow<List<FoodRating>>(emptyList())
        
        try {
            ratingsRef.orderByChild("foodId").equalTo(foodId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val ratings = mutableListOf<FoodRating>()
                        for (child in snapshot.children) {
                            val rating = child.getValue(FoodRating::class.java)
                            if (rating != null) {
                                ratings.add(rating)
                            }
                        }
                        ratingsFlow.value = ratings.sortedByDescending { it.createdAt }
                        Log.d(TAG, "Got ${ratings.size} ratings for food $foodId")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error getting ratings: ${error.message}")
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up listener: ${e.message}", e)
        }
        
        return ratingsFlow
    }

    override suspend fun getAverageRating(foodId: String): Flow<Float> {
        val averageFlow = MutableStateFlow(0f)
        
        try {
            ratingsRef.orderByChild("foodId").equalTo(foodId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var totalRating = 0f
                        var count = 0
                        for (child in snapshot.children) {
                            val rating = child.getValue(FoodRating::class.java)
                            if (rating != null && rating.rating > 0) {
                                totalRating += rating.rating
                                count++
                            }
                        }
                        val average = if (count > 0) totalRating / count else 0f
                        averageFlow.value = average
                        Log.d(TAG, "Average rating for food $foodId: $average ($count ratings)")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error getting average: ${error.message}")
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up average listener: ${e.message}", e)
        }
        
        return averageFlow
    }

    override suspend fun getRatingCount(foodId: String): Flow<Int> {
        val countFlow = MutableStateFlow(0)
        
        try {
            ratingsRef.orderByChild("foodId").equalTo(foodId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var count = 0
                        for (child in snapshot.children) {
                            val rating = child.getValue(FoodRating::class.java)
                            if (rating != null && rating.rating > 0) {
                                count++
                            }
                        }
                        countFlow.value = count
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error getting count: ${error.message}")
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up count listener: ${e.message}", e)
        }
        
        return countFlow
    }

    override suspend fun hasUserRatedFood(userId: String, orderId: String, foodId: String): Boolean {
        return try {
            val snapshot = ratingsRef
                .orderByChild("userId")
                .equalTo(userId)
                .get()
                .await()
            
            for (child in snapshot.children) {
                val rating = child.getValue(FoodRating::class.java)
                if (rating != null && rating.orderId == orderId && rating.foodId == foodId) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if user rated: ${e.message}", e)
            false
        }
    }

    override suspend fun getRatingByFoodAndUser(foodId: String, userId: String): FoodRating? {
        return try {
            val snapshot = ratingsRef
                .orderByChild("userId")
                .equalTo(userId)
                .get()
                .await()
            
            for (child in snapshot.children) {
                val rating = child.getValue(FoodRating::class.java)
                if (rating != null && rating.foodId == foodId) {
                    return rating
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting rating by food and user: ${e.message}", e)
            null
        }
    }
}
