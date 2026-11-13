package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

object AccountStatus {
    const val INACTIVE = "INACTIVE"
    const val PENDING_APPROVAL = "PENDING_APPROVAL"
    const val ACTIVE = "ACTIVE"
    const val SUSPENDED = "SUSPENDED"
}

object TransactionType {
    const val ACTIVATION_DEPOSIT = "ACTIVATION_DEPOSIT"
    const val DEPOSIT = "DEPOSIT"
    const val COMMISSION = "COMMISSION"
    const val WITHDRAWAL = "WITHDRAWAL"
    const val REFERRAL_BONUS = "REFERRAL_BONUS"
}

object TransactionStatus {
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
}

@Entity(tableName = "users")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val gmail: String,
    val fullName: String,
    val phone: String = "",
    val city: String = "",
    val businessName: String = "",
    val status: String = AccountStatus.INACTIVE,
    val walletBalance: Double = 0.0,
    val commissionEarned: Double = 0.0,
    val totalDeposited: Double = 0.0,
    val referralCode: String,
    val referredBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userEmail: String,
    val type: String,
    val amount: Double,
    val commissionAmount: Double = 0.0,
    val status: String,
    val utrRef: String,
    val title: String,
    val subtitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "linked_accounts")
data class LinkedAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val accountType: String, // "UPI" or "BANK"
    val label: String,
    val upiId: String = "",
    val accountHolder: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val senderTag: String = "JIO-PNL",
    val title: String,
    val message: String,
    val isSmsAlert: Boolean = true,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_messages")
data class SupportMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userEmail: String,
    val senderRole: String, // "USER" or "ADMIN"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerUserId: String,
    val friendName: String,
    val friendEmail: String,
    val status: String, // "ACTIVE" or "PENDING"
    val bonusEarned: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "panel_config")
data class PanelConfigEntity(
    @PrimaryKey val id: Int = 1,
    val activationAmount: Double = 5000.0,
    val commissionRatePercent: Double = 15.0,
    val officialUpiId: String = "vishalkumar90@upi",
    val telegramHandle: String = "@Sainishaab9076",
    val adminPasskey: String = "SAINI@9076",
    val panelLiveStatus: String = "LIVE"
)
