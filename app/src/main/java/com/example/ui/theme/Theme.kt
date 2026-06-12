package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.adaptive.UIConfig

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    uiConfig: UIConfig = UIConfig(),
    content: @Composable () -> Unit,
) {
    val accentColor: Color = when (uiConfig.accentColor) {
        "amber" -> AmberZen
        "green" -> Color(0xFF00FF88)
        "white" -> Color(0xFFE0E0E0)
        else    -> CyberCyan
    }

    val colorScheme = darkColorScheme(
        primary      = accentColor,
        secondary    = accentColor,
        tertiary     = accentColor,
        background   = VoidBlack,
        surface      = VoidBlack,
        onPrimary    = VoidBlack,
        onSecondary  = VoidBlack,
        onTertiary   = VoidBlack,
        onBackground = accentColor,
        onSurface    = accentColor
    )

    val fs = uiConfig.fontSize
    val dynamicTypography = Typography(
        bodyLarge = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = FontWeight.Normal,
            fontSize      = fs.sp,
            lineHeight    = (fs * 1.5f).sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = FontWeight.Normal,
            fontSize      = (fs * 0.875f).sp,
            lineHeight    = (fs * 1.35f).sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = FontWeight.Normal,
            fontSize      = (fs * 0.75f).sp,
            lineHeight    = (fs * 1.25f).sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = FontWeight.Medium,
            fontSize      = (fs * 0.875f).sp,
            lineHeight    = (fs * 1.4f).sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = FontWeight.Medium,
            fontSize      = (fs * 0.75f).sp,
            lineHeight    = (fs * 1.3f).sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = FontWeight.Medium,
            fontSize      = (fs * 0.6875f).sp,
            lineHeight    = (fs * 1.2f).sp,
            letterSpacing = 0.5.sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = dynamicTypography,
        content     = content
    )
}
