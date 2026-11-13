package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.JioPanalRepository
import com.example.data.local.LinkedAccountEntity
import com.example.data.local.ReferralEntity
import com.example.data.local.UserProfileEntity
import com.example.localization.AppStrings
import com.example.ui.components.StatusPill
import com.example.ui.components.copyToClipboard
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianVoid

@Composable
fun AccountsAndReferralScreen(
    strings: AppStrings,
    user: UserProfileEntity,
    accounts: List<LinkedAccountEntity>,
    referrals: List<ReferralEntity>,
    onAddUpi: (String) -> Unit,
    onAddBank: (holder: String, bank: String, accNo: String, ifsc: String) -> Unit,
    onSetPrimary: (Long) -> Unit,
    onDeleteAccount: (Long) -> Unit,
    onAddReferralInvite: (friendName: String, friendEmail: String) -> Unit
) {
    val context = LocalContext.current
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        // Linked Payout Accounts Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.linkedAccountsTitle, style = MaterialTheme.typography.headlineMedium)
                Button(
                    onClick = { showAddAccountDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("open_add_account_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.addAccountBtn)
                }
            }
        }

        if (accounts.isEmpty()) {
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
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Payout Accounts Linked", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Add your personal UPI ID or Bank Account to receive 15% commission withdrawals.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(accounts, key = { it.id }) { acc ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(
                        1.dp,
                        if (acc.isPrimary) ElectricEmerald else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (acc.accountType == "UPI") Icons.Default.QrCode else Icons.Default.AccountBalance,
                            contentDescription = acc.accountType,
                            tint = if (acc.isPrimary) ElectricEmerald else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(acc.label, style = MaterialTheme.typography.titleMedium)
                                if (acc.isPrimary) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = ElectricEmerald.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            "PRIMARY",
                                            style = MaterialTheme.typography.labelSmall.copy(color = ElectricEmerald),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            if (acc.accountType == "BANK") {
                                Text(
                                    "${acc.accountHolder} • IFSC: ${acc.ifscCode}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    "Instant UPI Payout",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (!acc.isPrimary) {
                            IconButton(onClick = { onSetPrimary(acc.id) }) {
                                Icon(Icons.Default.Star, contentDescription = "Set Primary", tint = BrightGold)
                            }
                        }
                        IconButton(onClick = { onDeleteAccount(acc.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = CoralAlert)
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.widthIn(max = 600.dp))
        }

        // Referral System Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(strings.referralTitle, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.referralSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("YOUR REFERRAL CODE", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = user.referralCode,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = BrightGold
                                    )
                                )
                            }
                            OutlinedButton(
                                onClick = { copyToClipboard(context, "Referral Code", user.referralCode) }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.copyCodeBtn)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val shareText =
                                    "Join Jio Panal for live 15% commission payouts! Use my referral code ${user.referralCode} when signing in."
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, strings.shareInviteBtn))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricEmerald,
                                contentColor = ObsidianVoid
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("share_referral_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.shareInviteBtn, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showInviteDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("log_referral_button")
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log Invite")
                        }
                    }
                }
            }
        }

        if (referrals.isNotEmpty()) {
            item {
                Text(
                    "Invited Partners (${referrals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                )
            }
            items(referrals, key = { it.id }) { ref ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(ref.friendName, style = MaterialTheme.typography.titleMedium)
                            Text(ref.friendEmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "+₹${JioPanalRepository.formatAmount(ref.bonusEarned)}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = ElectricEmerald
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StatusPill(status = ref.status)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }
    }

    if (showAddAccountDialog) {
        AddPayoutAccountDialog(
            strings = strings,
            onDismiss = { showAddAccountDialog = false },
            onSaveUpi = { upi ->
                onAddUpi(upi)
                showAddAccountDialog = false
            },
            onSaveBank = { holder, bank, accNo, ifsc ->
                onAddBank(holder, bank, accNo, ifsc)
                showAddAccountDialog = false
            }
        )
    }

    if (showInviteDialog) {
        LogReferralDialog(
            onDismiss = { showInviteDialog = false },
            onSubmit = { name, email ->
                onAddReferralInvite(name, email)
                showInviteDialog = false
            }
        )
    }
}

@Composable
private fun AddPayoutAccountDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSaveUpi: (String) -> Unit,
    onSaveBank: (String, String, String, String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var upiInput by remember { mutableStateOf("") }
    var holderInput by remember { mutableStateOf("") }
    var bankInput by remember { mutableStateOf("") }
    var accountNumInput by remember { mutableStateOf("") }
    var ifscInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addAccountBtn) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0; errorMsg = null }, text = { Text(strings.addUpiTab) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; errorMsg = null }, text = { Text(strings.addBankTab) })
                }
                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = upiInput,
                        onValueChange = { upiInput = it; errorMsg = null },
                        label = { Text("Your UPI ID") },
                        placeholder = { Text("name@okaxis / name@ybl") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_upi_input")
                    )
                } else {
                    OutlinedTextField(
                        value = holderInput,
                        onValueChange = { holderInput = it; errorMsg = null },
                        label = { Text("Account Holder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bankInput,
                        onValueChange = { bankInput = it; errorMsg = null },
                        label = { Text("Bank Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = accountNumInput,
                        onValueChange = { accountNumInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Account Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ifscInput,
                        onValueChange = { ifscInput = it.uppercase().take(11) },
                        label = { Text("IFSC Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (errorMsg != null) {
                    Text(errorMsg!!, color = CoralAlert, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTab == 0) {
                        if (!upiInput.contains("@")) {
                            errorMsg = "Enter a valid UPI ID containing @"
                        } else {
                            onSaveUpi(upiInput)
                        }
                    } else {
                        if (holderInput.isBlank() || bankInput.isBlank() || accountNumInput.length < 6 || ifscInput.length < 6) {
                            errorMsg = "Fill all bank account fields accurately."
                        } else {
                            onSaveBank(holderInput, bankInput, accountNumInput, ifscInput)
                        }
                    }
                },
                modifier = Modifier.testTag("save_account_confirm_button")
            ) {
                Text("Save Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun LogReferralDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Partner Invite") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    label = { Text("Partner Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMsg = null },
                    label = { Text("Partner Gmail") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(errorMsg!!, color = CoralAlert, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || !email.contains("@")) {
                        errorMsg = "Enter partner name and valid Gmail."
                    } else {
                        onSubmit(name, email)
                    }
                }
            ) {
                Text("Invite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
