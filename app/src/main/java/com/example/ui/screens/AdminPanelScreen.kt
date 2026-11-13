package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.AccountStatus
import com.example.data.local.JioPanalRepository
import com.example.data.local.PanelConfigEntity
import com.example.data.local.SupportMessageEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserProfileEntity
import com.example.localization.AppStrings
import com.example.ui.components.StatusPill
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianVoid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    strings: AppStrings,
    isAdminUnlocked: Boolean,
    config: PanelConfigEntity,
    pendingTransactions: List<TransactionEntity>,
    allUsers: List<UserProfileEntity>,
    allSupportMessages: List<SupportMessageEntity>,
    onVerifyPasskey: (String) -> Boolean,
    onLockAdmin: () -> Unit,
    onApproveTx: (Long) -> Unit,
    onRejectTx: (Long) -> Unit,
    onUpdateUserStatus: (String, String) -> Unit,
    onCreditBonus: (String, Double, String) -> Unit,
    onReplySupport: (String, String, String) -> Unit,
    onSaveSettings: (Double, Double, String, String, String) -> Unit,
    onBroadcastSms: (String, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isAdminUnlocked) strings.adminDashboardTitle else strings.adminPasskeyTitle
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAdminUnlocked) {
                        IconButton(onClick = onLockAdmin, modifier = Modifier.testTag("lock_admin_button")) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Admin Vault")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (!isAdminUnlocked) {
            AdminPasskeyGate(
                strings = strings,
                onVerifyPasskey = onVerifyPasskey,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            AdminDashboardBody(
                strings = strings,
                config = config,
                pendingTransactions = pendingTransactions,
                allUsers = allUsers,
                allSupportMessages = allSupportMessages,
                onApproveTx = onApproveTx,
                onRejectTx = onRejectTx,
                onUpdateUserStatus = onUpdateUserStatus,
                onCreditBonus = onCreditBonus,
                onReplySupport = onReplySupport,
                onSaveSettings = onSaveSettings,
                onBroadcastSms = onBroadcastSms,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}

@Composable
private fun AdminPasskeyGate(
    strings: AppStrings,
    onVerifyPasskey: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var passkey by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(1.dp, ChampagneGold),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = BrightGold,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(strings.adminPasskeyTitle, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Master key required to approve ₹5,000 activations, manage users, and trigger live credit SMS.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = passkey,
                    onValueChange = { passkey = it; errorMsg = null },
                    label = { Text(strings.adminPasskeyHint) },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_passkey_input")
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Default Master Passkey: SAINI@9076 (changeable in Controls)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMsg!!, color = CoralAlert, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!onVerifyPasskey(passkey)) {
                            errorMsg = "Incorrect Admin Passkey."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = ObsidianVoid
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("unlock_admin_button")
                ) {
                    Text(strings.unlockAdminBtn, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardBody(
    strings: AppStrings,
    config: PanelConfigEntity,
    pendingTransactions: List<TransactionEntity>,
    allUsers: List<UserProfileEntity>,
    allSupportMessages: List<SupportMessageEntity>,
    onApproveTx: (Long) -> Unit,
    onRejectTx: (Long) -> Unit,
    onUpdateUserStatus: (String, String) -> Unit,
    onCreditBonus: (String, Double, String) -> Unit,
    onReplySupport: (String, String, String) -> Unit,
    onSaveSettings: (Double, Double, String, String, String) -> Unit,
    onBroadcastSms: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var bonusTargetUser by remember { mutableStateOf<UserProfileEntity?>(null) }

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("${strings.pendingApprovalsTab} (${pendingTransactions.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("${strings.usersControlTab} (${allUsers.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(strings.supportInboxTab) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text(strings.panelSettingsTab) }
            )
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (pendingTransactions.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
                                    Text("No Pending Approvals", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "When users submit ₹5,000 activation or deposits, approve or reject them here.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(pendingTransactions, key = { it.id }) { tx ->
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 600.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(tx.title, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "₹${JioPanalRepository.formatAmount(tx.amount)}",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                color = BrightGold
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("User: ${tx.userEmail}", style = MaterialTheme.typography.bodyMedium)
                                    Text("UTR: ${tx.utrRef}", style = MaterialTheme.typography.labelSmall)
                                    if (tx.commissionAmount > 0) {
                                        Text(
                                            "+₹${JioPanalRepository.formatAmount(tx.commissionAmount)} (15% Commission) will be credited on approval",
                                            style = MaterialTheme.typography.labelMedium.copy(color = ElectricEmerald)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Button(
                                            onClick = { onApproveTx(tx.id) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ElectricEmerald,
                                                contentColor = ObsidianVoid
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                                .testTag("admin_approve_tx_${tx.id}")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(strings.approveBtn, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { onRejectTx(tx.id) },
                                            border = BorderStroke(1.dp, CoralAlert),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralAlert),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                                .testTag("admin_reject_tx_${tx.id}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(strings.rejectBtn)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(allUsers, key = { it.userId }) { u ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(u.fullName, style = MaterialTheme.typography.titleMedium)
                                        Text(u.gmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    StatusPill(status = u.status)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Bal: ₹${JioPanalRepository.formatAmount(u.walletBalance)} • Comm: ₹${JioPanalRepository.formatAmount(u.commissionEarned)}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = ElectricEmerald
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onUpdateUserStatus(u.userId, AccountStatus.ACTIVE) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Set Active") }
                                    OutlinedButton(
                                        onClick = { onUpdateUserStatus(u.userId, AccountStatus.SUSPENDED) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Suspend") }
                                    Button(
                                        onClick = { bonusTargetUser = u },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Credit")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                var replyInput by remember { mutableStateOf("") }
                val latestUserMsg = allSupportMessages.firstOrNull { it.senderRole == "USER" } ?: allSupportMessages.firstOrNull()

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (latestUserMsg != null) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 600.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Quick Reply to ${latestUserMsg.userEmail}", style = MaterialTheme.typography.titleMedium)
                                    OutlinedTextField(
                                        value = replyInput,
                                        onValueChange = { replyInput = it },
                                        label = { Text("Admin Support Reply") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            if (replyInput.isNotBlank()) {
                                                onReplySupport(latestUserMsg.userId, latestUserMsg.userEmail, replyInput)
                                                replyInput = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Send Admin Reply")
                                    }
                                }
                            }
                        }
                    }

                    items(allSupportMessages, key = { it.id }) { msg ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "${msg.senderRole} • ${msg.userEmail}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (msg.senderRole == "ADMIN") ElectricEmerald else BrightGold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(msg.message, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            3 -> {
                var activationAmt by remember(config.activationAmount) { mutableStateOf(config.activationAmount.toInt().toString()) }
                var commRate by remember(config.commissionRatePercent) { mutableStateOf(config.commissionRatePercent.toInt().toString()) }
                var upiId by remember(config.officialUpiId) { mutableStateOf(config.officialUpiId) }
                var telegram by remember(config.telegramHandle) { mutableStateOf(config.telegramHandle) }
                var newPasskey by remember(config.adminPasskey) { mutableStateOf(config.adminPasskey) }

                var smsTitle by remember { mutableStateOf("Credit Alert • 15% Bonus Live") }
                var smsBody by remember { mutableStateOf("SMS JIO-PNL: Live panel payouts active! Deposit today and earn instant 15% commission.") }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Panel & Passkey Controls", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(
                                    value = activationAmt,
                                    onValueChange = { activationAmt = it.filter { c -> c.isDigit() } },
                                    label = { Text("Activation Amount (₹)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = commRate,
                                    onValueChange = { commRate = it.filter { c -> c.isDigit() } },
                                    label = { Text("Commission Rate (%)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = upiId,
                                    onValueChange = { upiId = it },
                                    label = { Text("Official UPI ID") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = telegram,
                                    onValueChange = { telegram = it },
                                    label = { Text("Telegram Handle") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = newPasskey,
                                    onValueChange = { newPasskey = it },
                                    label = { Text("Master Admin Passkey") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = {
                                        onSaveSettings(
                                            activationAmt.toDoubleOrNull() ?: 5000.0,
                                            commRate.toDoubleOrNull() ?: 15.0,
                                            upiId,
                                            telegram,
                                            newPasskey
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save Panel Controls")
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            border = BorderStroke(1.dp, ElectricEmerald.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Live Credit SMS Broadcast", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(
                                    value = smsTitle,
                                    onValueChange = { smsTitle = it },
                                    label = { Text("Alert Title") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = smsBody,
                                    onValueChange = { smsBody = it },
                                    label = { Text("SMS Message") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = { onBroadcastSms(smsTitle, smsBody) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricEmerald,
                                        contentColor = ObsidianVoid
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Campaign, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.sendBroadcastBtn, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    bonusTargetUser?.let { target ->
        var bonusAmount by remember { mutableStateOf("750") }
        var bonusNote by remember { mutableStateOf("15% Commission Credit") }
        AlertDialog(
            onDismissRequest = { bonusTargetUser = null },
            title = { Text("Credit ${target.fullName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bonusAmount,
                        onValueChange = { bonusAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Credit Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bonusNote,
                        onValueChange = { bonusNote = it },
                        label = { Text("Credit Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = bonusAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onCreditBonus(target.userId, amt, bonusNote)
                        }
                        bonusTargetUser = null
                    }
                ) { Text("Credit & Send SMS") }
            },
            dismissButton = {
                TextButton(onClick = { bonusTargetUser = null }) { Text("Cancel") }
            }
        )
    }
}
