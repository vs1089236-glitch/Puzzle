package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.JioPanalRepository
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.localization.AppStrings
import com.example.ui.components.StatusPill
import com.example.ui.components.copyToClipboard
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun HistoryScreen(
    strings: AppStrings,
    transactions: List<TransactionEntity>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedTx by remember { mutableStateOf<TransactionEntity?>(null) }

    val filtered = remember(transactions, searchQuery, selectedFilter) {
        transactions.filter { tx ->
            val matchesFilter = when (selectedFilter) {
                "COMMISSION" -> tx.type == TransactionType.COMMISSION || tx.type == TransactionType.REFERRAL_BONUS
                "DEPOSIT" -> tx.type == TransactionType.DEPOSIT
                "ACTIVATION" -> tx.type == TransactionType.ACTIVATION_DEPOSIT
                "WITHDRAWAL" -> tx.type == TransactionType.WITHDRAWAL
                else -> true
            }
            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                tx.title.lowercase().contains(q) ||
                tx.utrRef.lowercase().contains(q) ||
                tx.subtitle.lowercase().contains(q)
            matchesFilter && matchesSearch
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    Text(strings.recentTransactions, style = MaterialTheme.typography.headlineLarge)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = ElectricEmerald.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, ElectricEmerald.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.OfflinePin,
                                contentDescription = null,
                                tint = ElectricEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.offlineReadyBadge,
                                style = MaterialTheme.typography.labelSmall.copy(color = ElectricEmerald)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search UTR, commission, or title...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "All",
                        "COMMISSION" to "15% Comm",
                        "ACTIVATION" to "Activation",
                        "DEPOSIT" to "Deposits",
                        "WITHDRAWAL" to "Payouts"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
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
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching ledger entries", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "All transactions are automatically cached offline for instant access.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { tx ->
                TransactionRowCard(
                    tx = tx,
                    onClick = { selectedTx = tx },
                    modifier = Modifier.widthIn(max = 600.dp)
                )
            }
        }
    }

    selectedTx?.let { tx ->
        TransactionReceiptDialog(tx = tx, onDismiss = { selectedTx = null })
    }
}

@Composable
private fun TransactionReceiptDialog(tx: TransactionEntity, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Digital Ledger Receipt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusPill(status = tx.status)
                Text(tx.title, style = MaterialTheme.typography.titleLarge)
                Text(tx.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider()
                ReceiptRow("Amount", "₹${JioPanalRepository.formatAmount(tx.amount)}")
                if (tx.commissionAmount > 0 && tx.type != TransactionType.COMMISSION) {
                    ReceiptRow("15% Commission", "+₹${JioPanalRepository.formatAmount(tx.commissionAmount)}")
                }
                ReceiptRow("UTR / Ref", tx.utrRef)
                ReceiptRow("Timestamp", formatTimestamp(tx.timestamp))
                ReceiptRow("Cache Mode", "Offline Vault Synced")

                OutlinedButton(
                    onClick = { copyToClipboard(context, "UTR Ref", tx.utrRef) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy UTR Reference")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
