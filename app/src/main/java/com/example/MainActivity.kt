package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.localization.stringsFor
import com.example.ui.components.AnimatedSplashReveal
import com.example.ui.components.BiometricLockOverlay
import com.example.ui.components.LiveSmsAlertBanner
import com.example.ui.screens.AccountsAndReferralScreen
import com.example.ui.screens.ActivateAndPayScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SupportChatScreen
import com.example.ui.state.MainViewModel
import com.example.ui.theme.JioPanalTheme
import com.example.util.BiometricHelper
import com.example.util.NotificationHelper

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_ACTIVATE = "activate"
const val ROUTE_HISTORY = "history"
const val ROUTE_ACCOUNTS = "accounts"
const val ROUTE_PROFILE = "profile"
const val ROUTE_SUPPORT = "support"
const val ROUTE_NOTIFICATIONS = "notifications"
const val ROUTE_ADMIN = "admin"

private data class NavTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.ensureChannel(this)

        setContent {
            val viewModel: MainViewModel = viewModel()
            val userPrefs by viewModel.userPrefs.collectAsStateWithLifecycle()
            val liveBanner by viewModel.liveSmsBanner.collectAsStateWithLifecycle()

            JioPanalTheme(darkTheme = userPrefs.isDarkMode) {
                JioPanalRoot(
                    activity = this,
                    viewModel = viewModel,
                    liveBanner = liveBanner
                )
            }
        }
    }
}

@Composable
private fun JioPanalRoot(
    activity: FragmentActivity,
    viewModel: MainViewModel,
    liveBanner: com.example.ui.state.LiveSmsBannerState?
) {
    val context = LocalContext.current
    val userPrefs by viewModel.userPrefs.collectAsStateWithLifecycle()
    val panelConfig by viewModel.panelConfig.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val otpState by viewModel.otpState.collectAsStateWithLifecycle()
    val isBiometricUnlocked by viewModel.isBiometricUnlocked.collectAsStateWithLifecycle()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()

    val transactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val accounts by viewModel.linkedAccounts.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val supportMessages by viewModel.supportMessages.collectAsStateWithLifecycle()
    val referrals by viewModel.referrals.collectAsStateWithLifecycle()

    val pendingTxs by viewModel.pendingTransactions.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allSupportMsgs by viewModel.allSupportMessages.collectAsStateWithLifecycle()

    val strings = remember(userPrefs.language) { stringsFor(userPrefs.language) }
    var showSplash by remember { mutableStateOf(true) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(showSplash) {
        if (!showSplash && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                try {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } catch (_: Exception) {
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val activeUser = currentUser
        if (showSplash) {
            AnimatedSplashReveal(onFinished = { showSplash = false })
        } else if (activeUser == null) {
            AuthScreen(
                strings = strings,
                selectedLanguage = userPrefs.language,
                otpState = otpState,
                onLanguageSelected = viewModel::setLanguage,
                onGmailChange = viewModel::updateGmailInput,
                onReferralChange = viewModel::updateReferralInput,
                onOtpChange = viewModel::updateOtpInput,
                onSendOtp = viewModel::sendGmailOtp,
                onAutoFillOtp = viewModel::autoFillOtp,
                onVerifyOtp = viewModel::verifyOtpAndLogin,
                onResetOtp = viewModel::resetOtpFlow
            )
        } else if (userPrefs.biometricEnabled && !isBiometricUnlocked) {
            val biometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }
            val triggerPrompt = {
                BiometricHelper.showBiometricPrompt(
                    activity = activity,
                    title = "Jio Panal Security Lock",
                    subtitle = "Authenticate to access ${activeUser.gmail}",
                    negativeButtonText = strings.unlockWithPin,
                    onSuccess = { viewModel.unlockBiometricSession() },
                    onError = { }
                )
            }
            LaunchedEffect(Unit) {
                if (biometricAvailable) {
                    triggerPrompt()
                }
            }
            BiometricLockOverlay(
                userEmail = activeUser.gmail,
                expectedPin = userPrefs.securityPin,
                biometricAvailable = biometricAvailable,
                biometricLabel = strings.biometricUnlock,
                pinLabel = strings.unlockWithPin,
                onTriggerBiometric = triggerPrompt,
                onPinSuccess = viewModel::unlockBiometricSession
            )
        } else {
            val user = activeUser
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_DASHBOARD

            val navItems = listOf(
                NavTabItem(ROUTE_DASHBOARD, strings.navHome, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                NavTabItem(ROUTE_ACTIVATE, strings.navActivate, Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner),
                NavTabItem(ROUTE_HISTORY, strings.navHistory, Icons.Filled.History, Icons.Outlined.History),
                NavTabItem(ROUTE_ACCOUNTS, strings.navAccounts, Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
                NavTabItem(ROUTE_PROFILE, strings.navProfile, Icons.Filled.Person, Icons.Outlined.Person)
            )
            val showPrimaryChrome = currentRoute in navItems.map { it.route }

            fun navigateTab(route: String) {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavRail = maxWidth >= 600.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    if (useNavRail && showPrimaryChrome) {
                        NavigationRail(
                            modifier = Modifier
                                .fillMaxHeight()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                        ) {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTab(item.route) },
                                    icon = {
                                        Icon(
                                            if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = { Text(item.label) },
                                    modifier = Modifier.testTag("nav_rail_${item.route}")
                                )
                            }
                        }
                    }

                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            if (!useNavRail && showPrimaryChrome) {
                                NavigationBar {
                                    navItems.forEach { item ->
                                        val selected = currentRoute == item.route
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { navigateTab(item.route) },
                                            icon = {
                                                Icon(
                                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.label
                                                )
                                            },
                                            label = { Text(item.label, maxLines = 1) },
                                            modifier = Modifier.testTag("nav_bar_${item.route}")
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_DASHBOARD,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            composable(ROUTE_DASHBOARD) {
                                DashboardScreen(
                                    strings = strings,
                                    user = user,
                                    config = panelConfig,
                                    transactions = transactions,
                                    accounts = accounts,
                                    unreadNotifications = unreadCount,
                                    onOpenActivate = { navigateTab(ROUTE_ACTIVATE) },
                                    onOpenHistory = { navigateTab(ROUTE_HISTORY) },
                                    onOpenAccounts = { navigateTab(ROUTE_ACCOUNTS) },
                                    onOpenSupport = { navController.navigate(ROUTE_SUPPORT) },
                                    onOpenNotifications = { navController.navigate(ROUTE_NOTIFICATIONS) },
                                    onOpenAdmin = { navController.navigate(ROUTE_ADMIN) },
                                    onRequestWithdraw = viewModel::requestWithdrawal
                                )
                            }

                            composable(ROUTE_ACTIVATE) {
                                ActivateAndPayScreen(
                                    strings = strings,
                                    user = user,
                                    config = panelConfig,
                                    onSubmitPayment = viewModel::submitPaymentProof
                                )
                            }

                            composable(ROUTE_HISTORY) {
                                HistoryScreen(
                                    strings = strings,
                                    transactions = transactions
                                )
                            }

                            composable(ROUTE_ACCOUNTS) {
                                AccountsAndReferralScreen(
                                    strings = strings,
                                    user = user,
                                    accounts = accounts,
                                    referrals = referrals,
                                    onAddUpi = viewModel::addUpiAccount,
                                    onAddBank = viewModel::addBankAccount,
                                    onSetPrimary = viewModel::setPrimaryAccount,
                                    onDeleteAccount = viewModel::deleteAccount,
                                    onAddReferralInvite = viewModel::addReferralInvite
                                )
                            }

                            composable(ROUTE_PROFILE) {
                                ProfileScreen(
                                    strings = strings,
                                    user = user,
                                    prefs = userPrefs,
                                    onSaveProfile = viewModel::updateProfile,
                                    onToggleDarkMode = viewModel::setDarkMode,
                                    onToggleBiometric = viewModel::setBiometricEnabled,
                                    onUpdateSecurityPin = viewModel::setSecurityPin,
                                    onSelectLanguage = viewModel::setLanguage,
                                    onOpenAdminVault = { navController.navigate(ROUTE_ADMIN) },
                                    onSignOut = viewModel::signOut
                                )
                            }

                            composable(ROUTE_SUPPORT) {
                                SupportChatScreen(
                                    strings = strings,
                                    config = panelConfig,
                                    messages = supportMessages,
                                    onSendMessage = viewModel::sendUserSupportMessage,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(ROUTE_NOTIFICATIONS) {
                                NotificationsScreen(
                                    strings = strings,
                                    notifications = notifications,
                                    onMarkAllRead = viewModel::markAllNotificationsRead,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(ROUTE_ADMIN) {
                                AdminPanelScreen(
                                    strings = strings,
                                    isAdminUnlocked = isAdminUnlocked,
                                    config = panelConfig,
                                    pendingTransactions = pendingTxs,
                                    allUsers = allUsers,
                                    allSupportMessages = allSupportMsgs,
                                    onVerifyPasskey = viewModel::verifyAdminPasskey,
                                    onLockAdmin = viewModel::lockAdminVault,
                                    onApproveTx = viewModel::approveTransactionAsAdmin,
                                    onRejectTx = viewModel::rejectTransactionAsAdmin,
                                    onUpdateUserStatus = viewModel::updateUserStatusAsAdmin,
                                    onCreditBonus = viewModel::creditManualBonusAsAdmin,
                                    onReplySupport = viewModel::replySupportAsAdmin,
                                    onSaveSettings = viewModel::updatePanelSettingsAsAdmin,
                                    onBroadcastSms = viewModel::sendBroadcastSmsAsAdmin,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Global Real-Time Live Credit SMS & OTP Banner Overlay
        LiveSmsAlertBanner(
            banner = liveBanner,
            onDismiss = viewModel::dismissLiveSmsBanner,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
