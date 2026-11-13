package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun observeUser(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserOnce(userId: String): UserProfileEntity?

    @Query("SELECT * FROM users WHERE gmail = :gmail LIMIT 1")
    suspend fun getUserByGmail(gmail: String): UserProfileEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserProfileEntity)

    @Update
    suspend fun updateUser(user: UserProfileEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeUserTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun observePendingTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)
}

@Dao
interface LinkedAccountDao {
    @Query("SELECT * FROM linked_accounts WHERE userId = :userId ORDER BY isPrimary DESC, createdAt DESC")
    fun observeAccounts(userId: String): Flow<List<LinkedAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: LinkedAccountEntity): Long

    @Query("UPDATE linked_accounts SET isPrimary = 0 WHERE userId = :userId")
    suspend fun clearPrimary(userId: String)

    @Query("UPDATE linked_accounts SET isPrimary = 1 WHERE id = :accountId")
    suspend fun setPrimary(accountId: Long)

    @Query("DELETE FROM linked_accounts WHERE id = :accountId")
    suspend fun deleteById(accountId: Long)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId = 'ALL' ORDER BY timestamp DESC")
    fun observeNotifications(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE (userId = :userId OR userId = 'ALL') AND isRead = 0")
    fun observeUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId OR userId = 'ALL'")
    suspend fun markAllRead(userId: String)
}

@Dao
interface SupportDao {
    @Query("SELECT * FROM support_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun observeUserMessages(userId: String): Flow<List<SupportMessageEntity>>

    @Query("SELECT * FROM support_messages ORDER BY timestamp DESC")
    fun observeAllMessages(): Flow<List<SupportMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: SupportMessageEntity): Long
}

@Dao
interface ReferralDao {
    @Query("SELECT * FROM referrals WHERE ownerUserId = :userId ORDER BY timestamp DESC")
    fun observeReferrals(userId: String): Flow<List<ReferralEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(referral: ReferralEntity): Long
}

@Dao
interface PanelConfigDao {
    @Query("SELECT * FROM panel_config WHERE id = 1 LIMIT 1")
    fun observeConfig(): Flow<PanelConfigEntity?>

    @Query("SELECT * FROM panel_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigOnce(): PanelConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: PanelConfigEntity)
}
