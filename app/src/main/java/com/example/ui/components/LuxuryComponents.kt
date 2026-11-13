package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AccountStatus
import com.example.data.local.JioPanalRepository
import com.example.data.local.TransactionStatus
import com.example.localization.AppLanguage
import com.example.ui.state.LiveSmsBannerState
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.DeepSlate
import com.example.ui.theme.ElectricEmerald
import com.example.ui.theme.ElevatedCardDark
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.WarmAmber
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashReveal(onFinished: () -> Unit) {
    var startAnim by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.72f,
        animationSpec = tween(750, easing = FastOutSlowInEasing),
        label = "splash_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(700),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        startAnim = true
        for (step in 1..20) {
            delay(65L)
            progress = step / 20f
        }
        delay(200L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A2236),
                        ObsidianVoid
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .scale(scale)
                .alpha(alpha)
        ) {
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(BrightGold, ElectricEmerald)),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .background(Color(0xFF0A0D14)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.jio_panal_icon_1794581668880),
                    contentDescription = "Jio Panal Emblem",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "JIO PANAL",
                style = MaterialTheme.typography.displayMedium.copy(
                    letterSpacing = 4.sp,
                    color = BrightGold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = ElectricEmerald.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, ElectricEmerald.copy(alpha = 0.45f))
            ) {
                Text(
                    text = "PRIVATE 15% COMMISSION VAULT",
                    style = MaterialTheme.typography.labelSmall.copy(color = ElectricEmerald),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50)),
                color = ChampagneGold,
                trackColor = Color(0xFF1F2A3F)
            )
        }
    }
}

@Composable
fun LiveSmsAlertBanner(
    banner: LiveSmsBannerState?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = banner != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (banner != null) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF101929),
                border = BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(listOf(ElectricEmerald, BrightGold))
                ),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .clickable { onDismiss() }
                    .testTag("live_sms_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ElectricEmerald.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "Live SMS Alert",
                            tint = ElectricEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = banner.title,
                            style = MaterialTheme.typography.labelLarge.copy(color = BrightGold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = banner.message,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFE2E8F0)),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Alert",
                            tint = Color(0xFF9AA7BD)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: String, modifier: Modifier = Modifier) {
    val (bg, fg, label) = when (status) {
        AccountStatus.ACTIVE, TransactionStatus.APPROVED, "LIVE" ->
            Triple(ElectricEmerald.copy(alpha = 0.16f), ElectricEmerald, status)
        AccountStatus.PENDING_APPROVAL, TransactionStatus.PENDING ->
            Triple(WarmAmber.copy(alpha = 0.18f), WarmAmber, status.replace("_", " "))
        AccountStatus.SUSPENDED, TransactionStatus.REJECTED ->
            Triple(CoralAlert.copy(alpha = 0.18f), CoralAlert, status)
        else ->
            Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, status)
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = fg, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun LuxuryBalanceCard(
    walletBalance: Double,
    commissionEarned: Double,
    commissionRate: Double,
    accountStatus: String,
    balanceLabel: String,
    commissionLabel: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(listOf(ChampagneGold.copy(alpha = 0.65f), ElectricEmerald.copy(alpha = 0.4f)))
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF141D2F),
                            Color(0xFF0D1422),
                            Color(0xFF132625)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElectricEmerald.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE PANEL • ${commissionRate.toInt()}% TIER",
                            style = MaterialTheme.typography.labelSmall.copy(color = BrightGold)
                        )
                    }
                    StatusPill(status = accountStatus)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = balanceLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFA7B4C9))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${JioPanalRepository.formatAmount(walletBalance)}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0B111C).copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color(0xFF233048))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Commission",
                                tint = ElectricEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = commissionLabel,
                                style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFD0D9E8))
                            )
                        }
                        Text(
                            text = "+₹${JioPanalRepository.formatAmount(commissionEarned)}",
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
}

@Composable
fun QrActivationCard(
    upiId: String,
    amount: Double,
    title: String,
    copyLabel: String,
    payAppLabel: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "QR",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Official user-provided QR Code on crisp white card for reliable scanning
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                border = BorderStroke(2.dp, ChampagneGold.copy(alpha = 0.6f))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.upi_qr_code),
                    contentDescription = "Official Jio Panal UPI QR Code",
                    modifier = Modifier
                        .size(220.dp)
                        .padding(10.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "UPI ID",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = upiId,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            copyToClipboard(context, "UPI ID", upiId)
                        },
                        modifier = Modifier.testTag("copy_upi_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = copyLabel,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(copyLabel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val uri = Uri.parse(
                        "upi://pay?pa=$upiId&pn=Jio%20Panal&am=${amount.toInt()}&cu=INR&tn=Jio%20Panal%20Deposit"
                    )
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        copyToClipboard(context, "UPI ID", upiId)
                        Toast.makeText(
                            context,
                            "No UPI app found on device. UPI ID copied: $upiId",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("pay_via_upi_app_button")
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("$payAppLabel • ₹${JioPanalRepository.formatAmount(amount)}")
            }
        }
    }
}

@Composable
fun TelegramSupportCard(
    handle: String,
    title: String,
    buttonLabel: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cleanHandle = handle.removePrefix("@")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, Color(0xFF2AABEE).copy(alpha = 0.45f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2AABEE).copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Telegram",
                        tint = Color(0xFF2AABEE)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = handle,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            color = Color(0xFF2AABEE)
                        )
                    )
                }
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified Support",
                    tint = ElectricEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$cleanHandle"))
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        copyToClipboard(context, "Telegram Support", handle)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2AABEE),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("telegram_support_button")
            ) {
                Text(buttonLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun LanguageSwitcherRow(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppLanguage.entries.forEach { lang ->
            val isSelected = lang == selected
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .clickable { onSelect(lang) }
                    .testTag("lang_chip_${lang.code}")
            ) {
                Text(
                    text = lang.displayName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun BiometricLockOverlay(
    userEmail: String,
    expectedPin: String,
    biometricAvailable: Boolean,
    biometricLabel: String,
    pinLabel: String,
    onTriggerBiometric: () -> Unit,
    onPinSuccess: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(ChampagneGold.copy(alpha = 0.16f))
                    .border(1.dp, ChampagneGold.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security Lock",
                    tint = BrightGold,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "JIO PANAL SECURITY VAULT",
                style = MaterialTheme.typography.titleLarge.copy(color = BrightGold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9AA7BD))
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (biometricAvailable) {
                Button(
                    onClick = onTriggerBiometric,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricEmerald,
                        contentColor = ObsidianVoid
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("biometric_unlock_button")
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(biometricLabel, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                text = "$pinLabel (Default: 9076)",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (pinError) CoralAlert else Color(0xFF9AA7BD)
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(4) { index ->
                    val filled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (filled) BrightGold else Color(0xFF1F2A3F))
                            .border(1.dp, if (pinError) CoralAlert else ChampagneGold.copy(alpha = 0.4f), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "OK")
            )
            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    row.forEach { key ->
                        Surface(
                            shape = CircleShape,
                            color = if (key == "OK") ChampagneGold else ElevatedCardDark,
                            modifier = Modifier
                                .size(64.dp)
                                .clickable {
                                    pinError = false
                                    when (key) {
                                        "C" -> enteredPin = ""
                                        "OK" -> {
                                            if (enteredPin == expectedPin) {
                                                onPinSuccess()
                                            } else {
                                                pinError = true
                                                enteredPin = ""
                                            }
                                        }
                                        else -> {
                                            if (enteredPin.length < 4) {
                                                enteredPin += key
                                                if (enteredPin.length == 4 && enteredPin == expectedPin) {
                                                    onPinSuccess()
                                                }
                                            }
                                        }
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = if (key == "OK") ObsidianVoid else Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
}
