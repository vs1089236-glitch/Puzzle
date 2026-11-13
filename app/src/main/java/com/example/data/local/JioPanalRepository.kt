package com.example.data.local

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class JioPanalRepository(private val db: JioPanalDatabase) {

    private val userDao = db.userDao()
    private val transactionDao = db.transactionDao()
    private val accountDao = db.linkedAccountDao()
    private val notificationDao = db.notificationDao()
    private val supportDao = db.supportDao()
    private val referralDao = db.referralDao()
    private val configDao = db.panelConfigDao()

    suspend fun ensureInitialConfig(): PanelConfigEntity {
        val existing = configDao.getConfigOnce()
        if (existing != null) return existing
        val defaultConfig = PanelConfigEntity(
            id = 1,
            activationAmount = 5000.0,
            commissionRatePercent = 15.0,
            officialUpiId = "vishalkumar90@upi",
            telegramHandle = "@Sainishaab9076",
            adminPasskey = "SAINI@9076",
            panelLiveStatus = "LIVE"
        )
        configDao.upsertConfig(defaultConfig)
        return defaultConfig
    }

    fun observeConfig(): Flow<PanelConfigEntity> =
        configDao.observeConfig().map { it ?: PanelConfigEntity() }

    fun observeUser(userId: String): Flow<UserProfileEntity?> = userDao.observeUser(userId)

    fun observeAllUsers(): Flow<List<UserProfileEntity>> = userDao.observeAllUsers()

    fun observeUserTransactions(userId: String): Flow<List<TransactionEntity>> =
        transactionDao.observeUserTransactions(userId)

    fun observeAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.observeAllTransactions()

    fun observePendingTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.observePendingTransactions()

    fun observeAccounts(userId: String): Flow<List<LinkedAccountEntity>> =
        accountDao.observeAccounts(userId)

    fun observeNotifications(userId: String): Flow<List<NotificationEntity>> =
        notificationDao.observeNotifications(userId)

    fun observeUnreadCount(userId: String): Flow<Int> =
        notificationDao.observeUnreadCount(userId)

    fun observeSupportMessages(userId: String): Flow<List<SupportMessageEntity>> =
        supportDao.observeUserMessages(userId)

    fun observeAllSupportMessages(): Flow<List<SupportMessageEntity>> =
        supportDao.observeAllMessages()

    fun observeReferrals(userId: String): Flow<List<ReferralEntity>> =
        referralDao.observeReferrals(userId)

    suspend fun loginOrRegisterWithGmail(gmail: String, referredByCode: String = ""): UserProfileEntity {
        ensureInitialConfig()
        val normalized = gmail.trim().lowercase(Locale.US)
        val existing = userDao.getUserByGmail(normalized)
        if (existing != null) {
            notificationDao.insert(
                NotificationEntity(
                    userId = existing.userId,
                    senderTag = "JIO-SEC",
                    title = "Secure Login Verified",
                    message = "Gmail OTP verified for $normalized. Offline ledger synced."
                )
            )
            return existing
        }

        val uid = "JP_${normalized.hashCode().toUInt().toString(16).uppercase(Locale.US)}"
        val nameGuess = normalized.substringBefore("@")
            .replace(".", " ")
            .replace("_", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
            }
            .ifBlank { "Panel Partner" }

        val refCode = "JIO${(1000..9999).random()}${nameGuess.take(2).uppercase(Locale.US)}"
        val newUser = UserProfileEntity(
            userId = uid,
            gmail = normalized,
            fullName = nameGuess,
            status = AccountStatus.INACTIVE,
            walletBalance = 0.0,
            commissionEarned = 0.0,
            totalDeposited = 0.0,
            referralCode = refCode,
            referredBy = referredByCode.trim().uppercase(Locale.US)
        )
        userDao.upsertUser(newUser)

        notificationDao.insert(
            NotificationEntity(
                userId = uid,
                senderTag = "JIO-PNL",
                title = "Welcome to Jio Panal",
                message = "Account created for $normalized. Activate with ₹5,000 deposit to unlock live 15% commission."
            )
        )
        supportDao.insert(
            SupportMessageEntity(
                userId = uid,
                userEmail = normalized,
                senderRole = "ADMIN",
                message = "Welcome to Jio Panal 24/7 Concierge! Complete your ₹5,000 activation via vishalkumar90@upi or reach @Sainishaab9076 on Telegram for instant help."
            )
        )
        return newUser
    }

    suspend fun updateProfileDetails(
        userId: String,
        fullName: String,
        phone: String,
        city: String,
        businessName: String
    ) {
        val current = userDao.getUserOnce(userId) ?: return
        userDao.updateUser(
            current.copy(
                fullName = fullName.trim().ifBlank { current.fullName },
                phone = phone.trim(),
                city = city.trim(),
                businessName = businessName.trim()
            )
        )
    }

    suspend fun submitDepositOrActivation(
        userId: String,
        amount: Double,
        utrRef: String,
        isActivation: Boolean
    ): Pair<String, String> {
        val config = ensureInitialConfig()
        val user = userDao.getUserOnce(userId) ?: error("User not found")
        val commission = (amount * config.commissionRatePercent) / 100.0
        val cleanUtr = utrRef.trim().uppercase(Locale.US)

        val type = if (isActivation) TransactionType.ACTIVATION_DEPOSIT else TransactionType.DEPOSIT
        val title = if (isActivation) "Panel Activation Deposit" else "Wallet Top-up Deposit"
        val subtitle = "UPI: ${config.officialUpiId} • +${config.commissionRatePercent.toInt()}% Comm"

        transactionDao.insert(
            TransactionEntity(
                userId = user.userId,
                userEmail = user.gmail,
                type = type,
                amount = amount,
                commissionAmount = commission,
                status = TransactionStatus.PENDING,
                utrRef = cleanUtr,
                title = title,
                subtitle = subtitle
            )
        )

        if (isActivation && user.status != AccountStatus.ACTIVE) {
            userDao.updateUser(user.copy(status = AccountStatus.PENDING_APPROVAL))
        }

        val smsTitle = if (isActivation) "Activation Proof Submitted" else "Deposit Request Received"
        val smsBody = "JIO-PNL: ₹${formatAmount(amount)} (UTR: $cleanUtr) submitted for verification. Expected 15% Comm: ₹${formatAmount(commission)}."
        notificationDao.insert(
            NotificationEntity(
                userId = user.userId,
                senderTag = "JIO-PAY",
                title = smsTitle,
                message = smsBody,
                isSmsAlert = true
            )
        )
        return smsTitle to smsBody
    }

    suspend fun requestWithdrawal(
        userId: String,
        amount: Double,
        accountLabel: String
    ): Pair<String, String> {
        val user = userDao.getUserOnce(userId) ?: error("User not found")
        require(user.status == AccountStatus.ACTIVE) { "Activate your panel first to withdraw." }
        require(amount > 0 && user.walletBalance >= amount) { "Insufficient panel balance." }

        val ref = "WD${System.currentTimeMillis().toString().takeLast(10)}"
        userDao.updateUser(user.copy(walletBalance = user.walletBalance - amount))
        transactionDao.insert(
            TransactionEntity(
                userId = user.userId,
                userEmail = user.gmail,
                type = TransactionType.WITHDRAWAL,
                amount = amount,
                commissionAmount = 0.0,
                status = TransactionStatus.PENDING,
                utrRef = ref,
                title = "Payout Withdrawal",
                subtitle = "To: $accountLabel"
            )
        )

        val smsTitle = "Withdrawal Initiated"
        val smsBody = "JIO-PNL: Payout request ₹${formatAmount(amount)} to $accountLabel queued (Ref: $ref)."
        notificationDao.insert(
            NotificationEntity(
                userId = user.userId,
                senderTag = "JIO-PAY",
                title = smsTitle,
                message = smsBody,
                isSmsAlert = true
            )
        )
        return smsTitle to smsBody
    }

    suspend fun approveTransaction(txId: Long): Pair<String, String>? {
        val tx = transactionDao.getById(txId) ?: return null
        if (tx.status != TransactionStatus.PENDING) return null
        val user = userDao.getUserOnce(tx.userId) ?: return null

        transactionDao.update(tx.copy(status = TransactionStatus.APPROVED))

        return when (tx.type) {
            TransactionType.ACTIVATION_DEPOSIT, TransactionType.DEPOSIT -> {
                val newBalance = user.walletBalance + tx.amount + tx.commissionAmount
                val newCommission = user.commissionEarned + tx.commissionAmount
                val newDeposited = user.totalDeposited + tx.amount
                userDao.updateUser(
                    user.copy(
                        status = AccountStatus.ACTIVE,
                        walletBalance = newBalance,
                        commissionEarned = newCommission,
                        totalDeposited = newDeposited
                    )
                )
                if (tx.commissionAmount > 0) {
                    transactionDao.insert(
                        TransactionEntity(
                            userId = user.userId,
                            userEmail = user.gmail,
                            type = TransactionType.COMMISSION,
                            amount = tx.commissionAmount,
                            commissionAmount = tx.commissionAmount,
                            status = TransactionStatus.APPROVED,
                            utrRef = "COMM-${tx.utrRef.takeLast(6)}",
                            title = "15% Instant Commission",
                            subtitle = "Credited on ₹${formatAmount(tx.amount)} deposit"
                        )
                    )
                }
                val smsTitle = "Credit SMS • ₹${formatAmount(tx.amount + tx.commissionAmount)} Credited"
                val smsBody = "SMS JIO-PNL: A/c Credited INR ${formatAmount(tx.amount)} + 15% Comm INR ${formatAmount(tx.commissionAmount)}. Panel Status: ACTIVE. Avl Bal: INR ${formatAmount(newBalance)}. Ref: ${tx.utrRef}"
                notificationDao.insert(
                    NotificationEntity(
                        userId = user.userId,
                        senderTag = "JIO-SMS",
                        title = smsTitle,
                        message = smsBody,
                        isSmsAlert = true
                    )
                )
                smsTitle to smsBody
            }

            TransactionType.WITHDRAWAL -> {
                val smsTitle = "Payout Settled • ₹${formatAmount(tx.amount)}"
                val smsBody = "SMS JIO-PNL: Withdrawal INR ${formatAmount(tx.amount)} successfully transferred (${tx.subtitle}). Ref: ${tx.utrRef}"
                notificationDao.insert(
                    NotificationEntity(
                        userId = user.userId,
                        senderTag = "JIO-SMS",
                        title = smsTitle,
                        message = smsBody,
                        isSmsAlert = true
                    )
                )
                smsTitle to smsBody
            }

            else -> null
        }
    }

    suspend fun rejectTransaction(txId: Long): Pair<String, String>? {
        val tx = transactionDao.getById(txId) ?: return null
        if (tx.status != TransactionStatus.PENDING) return null
        val user = userDao.getUserOnce(tx.userId) ?: return null

        transactionDao.update(tx.copy(status = TransactionStatus.REJECTED))

        when (tx.type) {
            TransactionType.ACTIVATION_DEPOSIT -> {
                if (user.status == AccountStatus.PENDING_APPROVAL) {
                    userDao.updateUser(user.copy(status = AccountStatus.INACTIVE))
                }
            }
            TransactionType.WITHDRAWAL -> {
                userDao.updateUser(user.copy(walletBalance = user.walletBalance + tx.amount))
            }
        }

        val smsTitle = "Transaction Declined"
        val smsBody = "JIO-PNL: ${tx.title} ₹${formatAmount(tx.amount)} (Ref: ${tx.utrRef}) was declined by Admin. Contact @Sainishaab9076 for details."
        notificationDao.insert(
            NotificationEntity(
                userId = user.userId,
                senderTag = "JIO-ALT",
                title = smsTitle,
                message = smsBody,
                isSmsAlert = true
            )
        )
        return smsTitle to smsBody
    }

    suspend fun addLinkedAccount(
        userId: String,
        accountType: String,
        upiId: String = "",
        accountHolder: String = "",
        bankName: String = "",
        accountNumber: String = "",
        ifscCode: String = ""
    ) {
        val label = if (accountType == "UPI") {
            upiId.trim()
        } else {
            "${bankName.trim()} ••••${accountNumber.trim().takeLast(4)}"
        }
        accountDao.clearPrimary(userId)
        accountDao.insert(
            LinkedAccountEntity(
                userId = userId,
                accountType = accountType,
                label = label,
                upiId = upiId.trim(),
                accountHolder = accountHolder.trim(),
                bankName = bankName.trim(),
                accountNumber = accountNumber.trim(),
                ifscCode = ifscCode.trim().uppercase(Locale.US),
                isPrimary = true
            )
        )
    }

    suspend fun setPrimaryAccount(userId: String, accountId: Long) {
        accountDao.clearPrimary(userId)
        accountDao.setPrimary(accountId)
    }

    suspend fun deleteAccount(accountId: Long) {
        accountDao.deleteById(accountId)
    }

    suspend fun sendSupportMessage(userId: String, userEmail: String, senderRole: String, message: String) {
        if (message.isBlank()) return
        supportDao.insert(
            SupportMessageEntity(
                userId = userId,
                userEmail = userEmail,
                senderRole = senderRole,
                message = message.trim()
            )
        )
        if (senderRole == "ADMIN") {
            notificationDao.insert(
                NotificationEntity(
                    userId = userId,
                    senderTag = "SUPPORT",
                    title = "Admin Reply Received",
                    message = message.trim(),
                    isSmsAlert = false
                )
            )
        }
    }

    suspend fun addReferralInvite(ownerUserId: String, friendName: String, friendEmail: String): Pair<String, String> {
        val config = ensureInitialConfig()
        val bonus = (config.activationAmount * config.commissionRatePercent) / 100.0
        referralDao.insert(
            ReferralEntity(
                ownerUserId = ownerUserId,
                friendName = friendName.trim(),
                friendEmail = friendEmail.trim().lowercase(Locale.US),
                status = "INVITED",
                bonusEarned = bonus
            )
        )
        val title = "Referral Invite Logged"
        val body = "Invited ${friendName.trim()}. You earn ₹${formatAmount(bonus)} (15%) when they activate!"
        notificationDao.insert(
            NotificationEntity(
                userId = ownerUserId,
                senderTag = "JIO-REF",
                title = title,
                message = body,
                isSmsAlert = false
            )
        )
        return title to body
    }

    suspend fun markAllNotificationsRead(userId: String) {
        notificationDao.markAllRead(userId)
    }

    suspend fun updateUserStatusByAdmin(userId: String, newStatus: String): Pair<String, String>? {
        val user = userDao.getUserOnce(userId) ?: return null
        userDao.updateUser(user.copy(status = newStatus))
        val title = "Panel Status Updated"
        val body = "SMS JIO-PNL: Your account status is now $newStatus via Admin Control."
        notificationDao.insert(
            NotificationEntity(
                userId = user.userId,
                senderTag = "JIO-ADM",
                title = title,
                message = body,
                isSmsAlert = true
            )
        )
        return title to body
    }

    suspend fun creditManualCommissionByAdmin(userId: String, amount: Double, note: String): Pair<String, String>? {
        val user = userDao.getUserOnce(userId) ?: return null
        val newBalance = user.walletBalance + amount
        val newComm = user.commissionEarned + amount
        userDao.updateUser(user.copy(walletBalance = newBalance, commissionEarned = newComm))

        val ref = "ADM${System.currentTimeMillis().toString().takeLast(8)}"
        transactionDao.insert(
            TransactionEntity(
                userId = user.userId,
                userEmail = user.gmail,
                type = TransactionType.COMMISSION,
                amount = amount,
                commissionAmount = amount,
                status = TransactionStatus.APPROVED,
                utrRef = ref,
                title = note.ifBlank { "15% Admin Credit Bonus" },
                subtitle = "Direct Admin Settlement"
            )
        )
        val title = "Credit SMS • ₹${formatAmount(amount)} Received"
        val body = "SMS JIO-PNL: A/c Credited INR ${formatAmount(amount)} (${note.ifBlank { "Commission Credit" }}). Avl Bal: INR ${formatAmount(newBalance)}. Ref: $ref"
        notificationDao.insert(
            NotificationEntity(
                userId = user.userId,
                senderTag = "JIO-SMS",
                title = title,
                message = body,
                isSmsAlert = true
            )
        )
        return title to body
    }

    suspend fun updatePanelSettings(
        activationAmount: Double,
        commissionRate: Double,
        upiId: String,
        telegramHandle: String,
        adminPasskey: String
    ) {
        val current = ensureInitialConfig()
        configDao.upsertConfig(
            current.copy(
                activationAmount = activationAmount,
                commissionRatePercent = commissionRate,
                officialUpiId = upiId.trim(),
                telegramHandle = telegramHandle.trim(),
                adminPasskey = adminPasskey.trim().ifBlank { current.adminPasskey }
            )
        )
    }

    suspend fun broadcastSmsAlert(title: String, message: String) {
        notificationDao.insert(
            NotificationEntity(
                userId = "ALL",
                senderTag = "JIO-SMS",
                title = title.trim(),
                message = message.trim(),
                isSmsAlert = true
            )
        )
    }

    companion object {
        fun formatAmount(amount: Double): String {
            return if (amount % 1.0 == 0.0) {
                String.format(Locale.US, "%,.0f", amount)
            } else {
                String.format(Locale.US, "%,.2f", amount)
            }
        }

        @Volatile
        private var INSTANCE: JioPanalRepository? = null

        fun getInstance(context: Context): JioPanalRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: JioPanalRepository(JioPanalDatabase.getInstance(context)).also { INSTANCE = it }
            }
        }
    }
}
