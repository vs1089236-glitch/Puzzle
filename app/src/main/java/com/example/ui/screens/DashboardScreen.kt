package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AccountStatus
import com.example.data.local.JioPanalRepository
import com.example.data.local.LinkedAccountEntity
import com.example.data.local.PanelConfigEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.local.UserProfileEntity
import com.example.localization.AppStrings
import com.example.ui.components.LuxuryBalanceCard
import com.example.ui.components.StatusPill
import com.example.ui.components.TelegramSupportCard
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.WarmAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    strings: AppStrings,
    user: UserProfileEntity,
    config: PanelConfigEntity,
    transactions: List<TransactionEntity>,
    accounts: List<LinkedAccountEntity>,
    unreadNotifications: Int,
    onOpenActivate: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAdmin: () -> Unit,
    onRequestWithdraw: (Double, String, (Result<Unit>) -> Unit) -> Unit
) {
    var calcAmountText by remember { mutableStateOf("5000") }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    val calcAmount = calcAmountText.toDoubleOrNull() ?: 0.0
    val estimatedCommission = (calcAmount * config.commissionRatePercent) / 100.0

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        // Luxury Header Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.jio_panal_icon_1794581668880),
                        contentDescription = "Jio Panal",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.appTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.testTag("notifications_bell_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge(containerColor = CoralAlert) {
                                        Text("$unreadNotifications", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenSupport,
                        modifier = Modifier.testTag("header_support_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "24/7 Support"
                        )
                    }

                    IconButton(
                        onClick = onOpenAdmin,
                        modifier = Modifier.testTag("header_admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Vault",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Main Luxury Balance Card
        item {
            LuxuryBalanceCard(
                walletBalance = user.walletBalance,
                commissionEarned = user.commissionEarned,
                commissionRate = config.commissionRatePercent,
                accountStatus = user.status,
                balanceLabel = strings.totalBalance,
                commissionLabel = strings.commissionEarned,
                modifier = Modifier.widthIn(max = 600.dp)
            )
        }

        // Dynamic Activation CTA Banner when account isn't ACTIVE yet
        if (user.status != AccountStatus.ACTIVE) {
            item {
                val isPending = user.status == AccountStatus.PENDING_APPROVAL
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPending) WarmAmber.copy(alpha = 0.14f) else MaterialTheme.colorScheme.primaryContainer
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isPending) WarmAmber else ChampagneGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isPending) "₹${JioPanalRepository.formatAmount(config.activationAmount)} Deposit Under Review" else strings.activateNowTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (isPending) WarmAmber else MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isPending)
                                "Your UTR proof is queued for Admin verification. Once approved, status turns ACTIVE and 15% commission unlocks instantly."
                            else
                                strings.activateNowDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onOpenActivate,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("dashboard_activate_cta")
                        ) {
                            Text(if (isPending) "View Payment / Submit Another" else strings.activateCta)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Quick Actions Grid
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    icon = Icons.Default.AddCard,
                    label = strings.quickDeposit,
                    accent = ElectricEmerald,
                    onClick = onOpenActivate,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_deposit_btn")
                )
                QuickActionTile(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = strings.quickWithdraw,
                    accent = BrightGold,
                    onClick = { showWithdrawDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_withdraw_btn")
                )
                QuickActionTile(
                    icon = Icons.Default.AccountBalance,
                    label = strings.quickAddAccount,
                    accent = Color(0xFF60A5FA),
                    onClick = onOpenAccounts,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_accounts_btn")
                )
                QuickActionTile(
                    icon = Icons.Default.SupportAgent,
                    label = strings.quickSupport,
                    accent = Color(0xFF2AABEE),
                    onClick = onOpenSupport,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_support_btn")
                )
            }
        }

        // Interactive 15% Commission Calculator
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.commissionCalcTitle, style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = calcAmountText,
                        onValueChange = { input -> calcAmountText = input.filter { it.isDigit() }.take(7) },
                        label = { Text(strings.enterAmountHint) },
                        prefix = { Text("₹ ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("commission_calc_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1000", "5000", "10000", "25000").forEach { preset ->
                            SuggestionChip(
                                onClick = { calcAmountText = preset },
                                label = { Text("₹$preset") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = strings.youEarn15,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Total Credit: ₹${JioPanalRepository.formatAmount(calcAmount + estimatedCommission)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Text(
                                text = "+₹${JioPanalRepository.formatAmount(estimatedCommission)}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Direct Telegram Support Card
        item {
            TelegramSupportCard(
                handle = config.telegramHandle,
                title = strings.telegramSupportTitle,
                buttonLabel = strings.telegramSupportBtn,
                modifier = Modifier.widthIn(max = 600.dp)
            )
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.recentTransactions, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.OfflinePin,
                        contentDescription = strings.offlineReadyBadge,
                        tint = ElectricEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
                TextButton(onClick = onOpenHistory) {
                    Text(strings.viewAll)
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No transactions yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Submit ₹5,000 activation or a deposit to start earning 15% commission.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(transactions.take(4), key = { it.id }) { tx ->
                TransactionRowCard(
                    tx = tx,
                    onClick = onOpenHistory,
                    modifier = Modifier.widthIn(max = 600.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }

    if (showWithdrawDialog) {
        WithdrawDialog(
            user = user,
            accounts = accounts,
            onDismiss = { showWithdrawDialog = false },
            onOpenAddAccount = {
                showWithdrawDialog = false
                onOpenAccounts()
            },
            onConfirmWithdraw = { amount, accountLabel, onDone ->
                onRequestWithdraw(amount, accountLabel, onDone)
            }
        )
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .height(88.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TransactionRowCard(
    tx: TransactionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDebit = tx.type == TransactionType.WITHDRAWAL
    val isCommission = tx.type == TransactionType.COMMISSION || tx.type == TransactionType.REFERRAL_BONUS
    val amountColor = when {
        isDebit -> CoralAlert
        isCommission -> ElectricEmerald
        else -> MaterialTheme.colorScheme.onSurface
    }
    val icon = when {
        isDebit -> Icons.Default.ArrowUpward
        isCommission -> Icons.Default.Bolt
        else -> Icons.Default.ArrowDownward
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("tx_card_${tx.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(amountColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = tx.title, tint = amountColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${tx.utrRef} • ${formatTimestamp(tx.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isDebit) "-" else "+"}₹${JioPanalRepository.formatAmount(tx.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        color = amountColor,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusPill(status = tx.status)
            }
        }
    }
}

@Composable
private fun WithdrawDialog(
    user: UserProfileEntity,
    accounts: List<LinkedAccountEntity>,
    onDismiss: () -> Unit,
    onOpenAddAccount: () -> Unit,
    onConfirmWithdraw: (Double, String, (Result<Unit>) -> Unit) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Payout Withdrawal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Available Balance: ₹${JioPanalRepository.formatAmount(user.walletBalance)}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        color = ElectricEmerald
                    )
                )
                if (user.status != AccountStatus.ACTIVE) {
                    Text(
                        text = "Account must be ACTIVE before withdrawing. Complete ₹5,000 activation first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmAmber
                    )
                } else if (accounts.isEmpty()) {
                    Text(
                        text = "Please add a UPI ID or Bank Account first to receive payouts.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(onClick = onOpenAddAccount, modifier = Modifier.fillMaxWidth()) {
                        Text("Add UPI / Bank Account")
                    }
                } else {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Withdrawal Amount (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Payout To:", style = MaterialTheme.typography.labelMedium)
                    accounts.forEach { acc ->
                        val chosen = selectedAccount?.id == acc.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (chosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAccount = acc }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${acc.accountType}: ${acc.label}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                if (errorMsg != null) {
                    Text(errorMsg!!, color = CoralAlert, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            if (user.status == AccountStatus.ACTIVE && accounts.isNotEmpty()) {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        val target = selectedAccount
                        if (amt <= 0 || target == null) {
                            errorMsg = "Enter a valid amount."
                        } else {
                            onConfirmWithdraw(amt, "${target.accountType} (${target.label})") { res ->
                                res.onSuccess { onDismiss() }
                                    .onFailure { errorMsg = it.message }
                            }
                        }
                    }
                ) {
                    Text("Withdraw")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
    return sdf.format(Date(millis))
}
