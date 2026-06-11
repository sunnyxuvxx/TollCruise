package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberTealDark,
    secondary = IndigoPurpleDark,
    tertiary = RosePinkDark,
    background = DeepSpaceBg,
    surface = DeepSurfaceGrey,
    onPrimary = Color(0xFF030712),
    onSecondary = Color.White,
    onBackground = Color(0xFFF3F4F6),
    onSurface = Color(0xFFF9FAFB),
    primaryContainer = Color(0xFF115E59), // Deep Emerald
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondaryContainer = Color(0xFF1E1B4B),
    onSecondaryContainer = Color(0xFFE0E7FF),
    surfaceVariant = DeepSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = DeepTealLight,
    secondary = RichVioletLight,
    tertiary = CrimsonRedLight,
    background = CleanOffWhiteBg,
    surface = CrispPureWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF1F2937),
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF115E59),
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF1E1B4B),
    surfaceVariant = CleanGreyVariant
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to showcase our beautiful brand identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
