package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.AccountStatus
import com.example.data.local.JioPanalRepository
import com.example.data.local.PanelConfigEntity
import com.example.data.local.UserProfileEntity
import com.example.localization.AppStrings
import com.example.ui.components.QrActivationCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianVoid

@Composable
fun ActivateAndPayScreen(
    strings: AppStrings,
    user: UserProfileEntity,
    config: PanelConfigEntity,
    onSubmitPayment: (amount: Double, utrRef: String, isActivation: Boolean, onSuccess: () -> Unit) -> Unit
) {
    var isActivationMode by remember(user.status) {
        mutableStateOf(user.status != AccountStatus.ACTIVE)
    }
    var customAmountText by remember { mutableStateOf("5000") }
    var utrInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var submittedNotice by remember { mutableStateOf<String?>(null) }

    val activeAmount = if (isActivationMode) config.activationAmount else (customAmountText.toDoubleOrNull() ?: 0.0)
    val commissionPreview = (activeAmount * config.commissionRatePercent) / 100.0

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isActivationMode) strings.activationAmountLabel else "Payment Gateway",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    StatusPill(status = user.status)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pay via official UPI or scan QR code, then submit UTR for live Admin approval & 15% commission.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Mode Switcher: ₹5,000 Activation vs Wallet Deposit
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isActivationMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { isActivationMode = true; errorMsg = null }
                        .testTag("mode_activation_tab")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = if (isActivationMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.height(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Activate ₹${config.activationAmount.toInt()}",
                            color = if (isActivationMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (!isActivationMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { isActivationMode = false; errorMsg = null }
                        .testTag("mode_deposit_tab")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (!isActivationMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.height(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Deposit (+15%)",
                            color = if (!isActivationMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Amount + 15% Commission Preview Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isActivationMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.activationAmountLabel, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "₹${JioPanalRepository.formatAmount(config.activationAmount)}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = customAmountText,
                            onValueChange = { customAmountText = it.filter { c -> c.isDigit() }.take(7) },
                            label = { Text("Deposit Amount (₹)") },
                            prefix = { Text("₹ ") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deposit_amount_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1000", "2500", "5000", "10000").forEach { preset ->
                                SuggestionChip(
                                    onClick = { customAmountText = preset },
                                    label = { Text("₹$preset") }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ElectricEmerald.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, ElectricEmerald.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+${config.commissionRatePercent.toInt()}% Commission Bonus",
                                style = MaterialTheme.typography.labelLarge.copy(color = ElectricEmerald)
                            )
                            Text(
                                text = "+₹${JioPanalRepository.formatAmount(commissionPreview)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = ElectricEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Official QR Code & UPI Card (vishalkumar90@upi + user QR)
        item {
            QrActivationCard(
                upiId = config.officialUpiId,
                amount = activeAmount.coerceAtLeast(1.0),
                title = strings.scanQrOrUpi,
                copyLabel = strings.copyUpi,
                payAppLabel = strings.payViaUpiApp,
                modifier = Modifier.widthIn(max = 600.dp)
            )
        }

        // UTR Proof Submission Card
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
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm Payment Proof", style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = utrInput,
                        onValueChange = { utrInput = it.uppercase().take(20); errorMsg = null },
                        label = { Text(strings.enterUtrLabel) },
                        placeholder = { Text("e.g. 431829041289") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("utr_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SuggestionChip(
                        onClick = {
                            utrInput = "UTR${System.currentTimeMillis().toString().takeLast(9)}"
                            errorMsg = null
                        },
                        label = { Text("Generate Test UTR") }
                    )

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMsg!!, color = CoralAlert, style = MaterialTheme.typography.bodyMedium)
                    }

                    if (submittedNotice != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElectricEmerald.copy(alpha = 0.14f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = submittedNotice!!,
                                color = ElectricEmerald,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (activeAmount <= 0) {
                                errorMsg = "Enter a valid deposit amount."
                            } else if (utrInput.trim().length < 6) {
                                errorMsg = "Enter a valid UTR / Reference number (min 6 characters)."
                            } else {
                                onSubmitPayment(activeAmount, utrInput, isActivationMode) {
                                    submittedNotice = "Submitted! Open Admin Vault (Shield icon) to approve and trigger live 15% credit SMS."
                                    utrInput = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricEmerald,
                            contentColor = ObsidianVoid
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_payment_proof_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isActivationMode) strings.submitActivation else strings.submitDeposit,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }
    }
}
