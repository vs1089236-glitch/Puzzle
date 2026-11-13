package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val JioPanalDarkColorScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = ObsidianVoid,
    primaryContainer = DeepGoldContainer,
    onPrimaryContainer = BrightGold,
    secondary = ElectricEmerald,
    onSecondary = ObsidianVoid,
    secondaryContainer = EmeraldDim,
    onSecondaryContainer = ElectricEmerald,
    tertiary = WarmAmber,
    onTertiary = ObsidianVoid,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = WarmAmber,
    background = ObsidianVoid,
    onBackground = TextPrimaryDark,
    surface = DeepSlate,
    onSurface = TextPrimaryDark,
    surfaceVariant = ElevatedCardDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainer = ElevatedCardDark,
    surfaceContainerHigh = ElevatedCardDarkAlt,
    outline = SlateBorderDark,
    error = CoralAlert,
    onError = Color.White,
    errorContainer = CoralContainer,
    onErrorContainer = CoralAlert
)

private val JioPanalLightColorScheme = lightColorScheme(
    primary = RichGoldLight,
    onPrimary = CrispWhite,
    primaryContainer = Color(0xFFF6EBCB),
    onPrimaryContainer = Color(0xFF433202),
    secondary = RichEmeraldLight,
    onSecondary = CrispWhite,
    secondaryContainer = Color(0xFFD6F5EA),
    onSecondaryContainer = Color(0xFF004731),
    tertiary = Color(0xFFB86E00),
    onTertiary = CrispWhite,
    tertiaryContainer = Color(0xFFFFE7C4),
    onTertiaryContainer = Color(0xFF4A2B00),
    background = SoftIvory,
    onBackground = TextPrimaryLight,
    surface = CrispWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = SoftWarmSurface,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainer = CrispWhite,
    surfaceContainerHigh = SoftWarmSurface,
    outline = SlateBorderLight,
    error = Color(0xFFD32F46),
    onError = CrispWhite,
    errorContainer = Color(0xFFFFE5E8),
    onErrorContainer = Color(0xFF6B0B19)
)

val JioPanalShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun JioPanalTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) JioPanalDarkColorScheme else JioPanalLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = JioPanalTypography,
        shapes = JioPanalShapes,
        content = content
    )
}
