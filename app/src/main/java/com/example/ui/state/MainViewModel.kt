package com.example.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.JioPanalRepository
import com.example.data.local.LinkedAccountEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PanelConfigEntity
import com.example.data.local.ReferralEntity
import com.example.data.local.SupportMessageEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserPreferences
import com.example.data.local.UserPrefsState
import com.example.data.local.UserProfileEntity
import com.example.localization.AppLanguage
import com.example.util.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.SecureRandom

data class LiveSmsBannerState(
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class OtpAuthState(
    val gmailInput: String = "",
    val referralInput: String = "",
    val otpSent: Boolean = false,
    val generatedOtp: String = "",
    val otpInput: String = "",
    val secondsRemaining: Int = 0,
    val errorMessage: String? = null,
    val isVerifying: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = JioPanalRepository.getInstance(application)
    private val prefs = UserPreferences(application)
    private val secureRandom = SecureRandom()

    val userPrefs: StateFlow<UserPrefsState> = prefs.prefsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserPrefsState())

    val panelConfig: StateFlow<PanelConfigEntity> = repo.observeConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), PanelConfigEntity())

    val currentUser: StateFlow<UserProfileEntity?> = userPrefs
        .flatMapLatest { state ->
            val uid = state.currentUserId
            if (uid == null) flowOf(null) else repo.observeUser(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val userTransactions: StateFlow<List<TransactionEntity>> = userPrefs
        .flatMapLatest { state ->
            val uid = state.currentUserId
            if (uid == null) flowOf(emptyList()) else repo.observeUserTransactions(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val linkedAccounts: StateFlow<List<LinkedAccountEntity>> = userPrefs
        .flatMapLatest { state ->
            val uid = state.currentUserId
            if (uid == null) flowOf(emptyList()) else repo.observeAccounts(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = userPrefs
        .flatMapLatest { state ->
            val uid = state.currentUserId
            if (uid == null) flowOf(emptyList()) else repo.observeNotifications(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val unreadNotificationCount: StateFlow<Int> = userPrefs
        .flatMapLatest { state ->
            val uid = state.currentUserId
            if (uid == null) flowOf(0) else repo.observeUnreadCount(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    val supportMessages: StateFlow<List<SupportMessageEntity>> = userPrefs
        .flatMapLatest { state ->
            val uid = state.currentUserId
            if (uid == null) flowOf(emptyList()) else repo.observeSupportMessages(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val referrals: StateFlow<List<ReferralEntity>> = userPrefs
        .flatMapLatest { state ->
            val uid = state.currentUserId
            if (uid == null) flowOf(emptyList()) else repo.observeReferrals(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Admin Flows
    val allUsers: StateFlow<List<UserProfileEntity>> = repo.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repo.observeAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val pendingTransactions: StateFlow<List<TransactionEntity>> = repo.observePendingTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allSupportMessages: StateFlow<List<SupportMessageEntity>> = repo.observeAllSupportMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _otpState = MutableStateFlow(OtpAuthState())
    val otpState: StateFlow<OtpAuthState> = _otpState.asStateFlow()

    private val _liveSmsBanner = MutableStateFlow<LiveSmsBannerState?>(null)
    val liveSmsBanner: StateFlow<LiveSmsBannerState?> = _liveSmsBanner.asStateFlow()

    private val _isBiometricUnlocked = MutableStateFlow(false)
    val isBiometricUnlocked: StateFlow<Boolean> = _isBiometricUnlocked.asStateFlow()

    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    init {
        viewModelScope.launch {
            repo.ensureInitialConfig()
        }
    }

    fun updateGmailInput(value: String) {
        _otpState.value = _otpState.value.copy(gmailInput = value, errorMessage = null)
    }

    fun updateReferralInput(value: String) {
        _otpState.value = _otpState.value.copy(referralInput = value)
    }

    fun updateOtpInput(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(6)
        _otpState.value = _otpState.value.copy(otpInput = digitsOnly, errorMessage = null)
    }

    fun sendGmailOtp() {
        val email = _otpState.value.gmailInput.trim()
        if (!email.contains("@") || !email.contains(".")) {
            _otpState.value = _otpState.value.copy(errorMessage = "Please enter a valid Gmail address.")
            return
        }
        val otp = (100000 + secureRandom.nextInt(900000)).toString()
        _otpState.value = _otpState.value.copy(
            otpSent = true,
            generatedOtp = otp,
            otpInput = "",
            secondsRemaining = 90,
            errorMessage = null
        )

        val ctx = getApplication<Application>()
        NotificationHelper.postLiveAlert(
            ctx,
            "Jio Panal Gmail OTP: $otp",
            "Use verification code $otp to sign in to $email."
        )
        showSmsBanner(
            title = "Gmail OTP • $otp",
            message = "Verification code $otp sent for $email. Tap Auto-Fill or enter below."
        )

        viewModelScope.launch {
            while (_otpState.value.secondsRemaining > 0 && _otpState.value.otpSent) {
                delay(1000L)
                _otpState.value = _otpState.value.copy(
                    secondsRemaining = (_otpState.value.secondsRemaining - 1).coerceAtLeast(0)
                )
            }
        }
    }

    fun autoFillOtp() {
        val code = _otpState.value.generatedOtp
        if (code.isNotEmpty()) {
            _otpState.value = _otpState.value.copy(otpInput = code, errorMessage = null)
        }
    }

    fun resetOtpFlow() {
        _otpState.value = OtpAuthState(gmailInput = _otpState.value.gmailInput)
    }

    fun verifyOtpAndLogin() {
        val state = _otpState.value
        if (state.otpInput.length != 6 || state.otpInput != state.generatedOtp) {
            _otpState.value = state.copy(errorMessage = "Invalid 6-digit OTP. Check the verification alert.")
            return
        }
        viewModelScope.launch {
            _otpState.value = state.copy(isVerifying = true, errorMessage = null)
            val user = repo.loginOrRegisterWithGmail(state.gmailInput, state.referralInput)
            prefs.setCurrentUserId(user.userId)
            _isBiometricUnlocked.value = true
            _otpState.value = OtpAuthState()
            showSmsBanner(
                title = "Login Verified • ${user.fullName}",
                message = "Welcome to Jio Panal! 15% Commission Tier active on panel."
            )
        }
    }

    fun unlockBiometricSession() {
        _isBiometricUnlocked.value = true
    }

    fun signOut() {
        viewModelScope.launch {
            prefs.setCurrentUserId(null)
            _isBiometricUnlocked.value = false
            _isAdminUnlocked.value = false
            _otpState.value = OtpAuthState()
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkMode(enabled) }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setBiometricEnabled(enabled) }
    }

    fun setSecurityPin(pin: String) {
        if (pin.length == 4) {
            viewModelScope.launch { prefs.setSecurityPin(pin) }
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { prefs.setLanguage(language) }
    }

    fun updateProfile(fullName: String, phone: String, city: String, businessName: String) {
        val uid = currentUser.value?.userId ?: return
        viewModelScope.launch {
            repo.updateProfileDetails(uid, fullName, phone, city, businessName)
            showSmsBanner("Profile Saved", "Personal details updated safely.")
        }
    }

    fun submitPaymentProof(amount: Double, utrRef: String, isActivation: Boolean, onSuccess: () -> Unit) {
        val uid = currentUser.value?.userId ?: return
        viewModelScope.launch {
            val (title, body) = repo.submitDepositOrActivation(uid, amount, utrRef, isActivation)
            dispatchLiveSms(title, body)
            onSuccess()
        }
    }

    fun requestWithdrawal(amount: Double, accountLabel: String, onResult: (Result<Unit>) -> Unit) {
        val uid = currentUser.value?.userId ?: return
        viewModelScope.launch {
            try {
                val (title, body) = repo.requestWithdrawal(uid, amount, accountLabel)
                dispatchLiveSms(title, body)
                onResult(Result.success(Unit))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun addUpiAccount(upiId: String) {
        val uid = currentUser.value?.userId ?: return
        viewModelScope.launch {
            repo.addLinkedAccount(uid, accountType = "UPI", upiId = upiId)
            showSmsBanner("UPI Added", "$upiId linked as your primary payout account.")
        }
    }

    fun addBankAccount(holder: String, bankName: String, accountNumber: String, ifsc: String) {
        val uid = currentUser.value?.userId ?: return
        viewModelScope.launch {
            repo.addLinkedAccount(
                userId = uid,
                accountType = "BANK",
                accountHolder = holder,
                bankName = bankName,
                accountNumber = accountNumber,
                ifscCode = ifsc
            )
            showSmsBanner("Bank Account Linked", "$bankName account added for payouts.")
        }
    }

    fun setPrimaryAccount(accountId: Long) {
        val uid = currentUser.value?.userId ?: return
        viewModelScope.launch { repo.setPrimaryAccount(uid, accountId) }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch { repo.deleteAccount(accountId) }
    }

    fun sendUserSupportMessage(text: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repo.sendSupportMessage(user.userId, user.gmail, "USER", text)
        }
    }

    fun addReferralInvite(friendName: String, friendEmail: String) {
        val uid = currentUser.value?.userId ?: return
        viewModelScope.launch {
            val (title, body) = repo.addReferralInvite(uid, friendName, friendEmail)
            dispatchLiveSms(title, body)
        }
    }

    fun markAllNotificationsRead() {
        val uid = currentUser.value?.userId ?: return
        viewModelScope.launch { repo.markAllNotificationsRead(uid) }
    }

    // Admin actions
    fun verifyAdminPasskey(enteredPasskey: String): Boolean {
        val valid = enteredPasskey.trim() == panelConfig.value.adminPasskey
        if (valid) {
            _isAdminUnlocked.value = true
        }
        return valid
    }

    fun lockAdminVault() {
        _isAdminUnlocked.value = false
    }

    fun approveTransactionAsAdmin(txId: Long) {
        viewModelScope.launch {
            val alert = repo.approveTransaction(txId)
            if (alert != null) {
                dispatchLiveSms(alert.first, alert.second)
            }
        }
    }

    fun rejectTransactionAsAdmin(txId: Long) {
        viewModelScope.launch {
            val alert = repo.rejectTransaction(txId)
            if (alert != null) {
                dispatchLiveSms(alert.first, alert.second)
            }
        }
    }

    fun updateUserStatusAsAdmin(userId: String, newStatus: String) {
        viewModelScope.launch {
            val alert = repo.updateUserStatusByAdmin(userId, newStatus)
            if (alert != null) {
                dispatchLiveSms(alert.first, alert.second)
            }
        }
    }

    fun creditManualBonusAsAdmin(userId: String, amount: Double, note: String) {
        viewModelScope.launch {
            val alert = repo.creditManualCommissionByAdmin(userId, amount, note)
            if (alert != null) {
                dispatchLiveSms(alert.first, alert.second)
            }
        }
    }

    fun replySupportAsAdmin(targetUserId: String, targetEmail: String, reply: String) {
        viewModelScope.launch {
            repo.sendSupportMessage(targetUserId, targetEmail, "ADMIN", reply)
            dispatchLiveSms("Support Reply Sent", reply)
        }
    }

    fun updatePanelSettingsAsAdmin(
        activationAmount: Double,
        commissionRate: Double,
        upiId: String,
        telegramHandle: String,
        newPasskey: String
    ) {
        viewModelScope.launch {
            repo.updatePanelSettings(activationAmount, commissionRate, upiId, telegramHandle, newPasskey)
            showSmsBanner("Panel Controls Updated", "Settings & Admin Passkey saved.")
        }
    }

    fun sendBroadcastSmsAsAdmin(title: String, message: String) {
        viewModelScope.launch {
            repo.broadcastSmsAlert(title, message)
            dispatchLiveSms(title, message)
        }
    }

    fun dismissLiveSmsBanner() {
        _liveSmsBanner.value = null
    }

    private fun showSmsBanner(title: String, message: String) {
        _liveSmsBanner.value = LiveSmsBannerState(title, message)
        viewModelScope.launch {
            val stamp = _liveSmsBanner.value?.timestamp
            delay(5200L)
            if (_liveSmsBanner.value?.timestamp == stamp) {
                _liveSmsBanner.value = null
            }
        }
    }

    private fun dispatchLiveSms(title: String, body: String) {
        val ctx = getApplication<Application>()
        NotificationHelper.triggerAlertHaptic(ctx)
        NotificationHelper.postLiveAlert(ctx, title, body)
        showSmsBanner(title, body)
    }
}
