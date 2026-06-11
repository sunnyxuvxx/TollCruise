package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_billing")
data class UserBilling(
    @PrimaryKey val id: Int = 1,
    val subscriptionPlan: String = "Free", // "Free", "Pro", "Ultra"
    val credits: Int = 15,          // Starter credits for "Free" tier users
    val subscriptionExpiry: Long = 0L      // End timestamp for subscriptions
)
