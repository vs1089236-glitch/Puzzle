package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        TransactionEntity::class,
        LinkedAccountEntity::class,
        NotificationEntity::class,
        SupportMessageEntity::class,
        ReferralEntity::class,
        PanelConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JioPanalDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun linkedAccountDao(): LinkedAccountDao
    abstract fun notificationDao(): NotificationDao
    abstract fun supportDao(): SupportDao
    abstract fun referralDao(): ReferralDao
    abstract fun panelConfigDao(): PanelConfigDao

    companion object {
        @Volatile
        private var INSTANCE: JioPanalDatabase? = null

        fun getInstance(context: Context): JioPanalDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    JioPanalDatabase::class.java,
                    "jio_panal_vault.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
