package ai.pleos.playground.location.fused.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Night,
    primaryContainer = DeepBlue,
    onPrimaryContainer = ColorTokens.OnDeepBlue,
    tertiary = Mint,
    background = Night,
    onBackground = Mist,
    surface = NightSurface,
    onSurface = Mist,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = Slate,
    error = Coral
)

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue,
    onPrimary = LightSurface,
    primaryContainer = ElectricBlue,
    onPrimaryContainer = Ink,
    tertiary = ColorTokens.DarkMint,
    background = LightBackground,
    onBackground = Ink,
    surface = LightSurface,
    onSurface = Ink,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = ColorTokens.LightSlate
)

private object ColorTokens {
    val OnDeepBlue = androidx.compose.ui.graphics.Color(0xFFE1F7FF)
    val DarkMint = androidx.compose.ui.graphics.Color(0xFF006B55)
    val LightSlate = androidx.compose.ui.graphics.Color(0xFF50636C)
}

@Composable
fun FusedLocationSampleAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
