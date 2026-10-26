package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.CyberTheme

fun getCyberColorScheme(theme: CyberTheme) = when (theme) {
    CyberTheme.MATRIX_GREEN -> darkColorScheme(
        primary = MatrixGreenPrimary,
        onPrimary = PureBlack,
        secondary = MatrixGreenSecondary,
        onSecondary = PureBlack,
        tertiary = MatrixGreenDim,
        background = MatrixDarkBg,
        onBackground = MatrixGreenPrimary,
        surface = MatrixSurface,
        onSurface = MatrixGreenPrimary,
        outline = MatrixBorder
    )
    CyberTheme.CYBERPUNK_NEON -> darkColorScheme(
        primary = CyberCyanPrimary,
        onPrimary = PureBlack,
        secondary = CyberMagenta,
        onSecondary = Color.White,
        tertiary = CyberPurple,
        background = CyberDarkBg,
        onBackground = TextHigh,
        surface = CyberSurface,
        onSurface = TextHigh,
        outline = CyberBorder
    )
    CyberTheme.BLOOD_RED -> darkColorScheme(
        primary = BloodRedPrimary,
        onPrimary = PureBlack,
        secondary = BloodRedSecondary,
        onSecondary = PureBlack,
        tertiary = Color(0xFFFF8A80),
        background = BloodDarkBg,
        onBackground = Color(0xFFFFD5D9),
        surface = BloodSurface,
        onSurface = Color(0xFFFFD5D9),
        outline = BloodBorder
    )
    CyberTheme.AMBER_CRT -> darkColorScheme(
        primary = AmberPrimary,
        onPrimary = PureBlack,
        secondary = AmberSecondary,
        onSecondary = PureBlack,
        tertiary = Color(0xFFFFD54F),
        background = AmberDarkBg,
        onBackground = AmberPrimary,
        surface = AmberSurface,
        onSurface = AmberPrimary,
        outline = AmberBorder
    )
    CyberTheme.DRACULA_VIOLET -> darkColorScheme(
        primary = DraculaVioletPrimary,
        onPrimary = PureBlack,
        secondary = DraculaGreen,
        onSecondary = PureBlack,
        tertiary = DraculaCyan,
        background = DraculaDarkBg,
        onBackground = TextHigh,
        surface = DraculaSurface,
        onSurface = TextHigh,
        outline = DraculaBorder
    )
}

@Composable
fun MyApplicationTheme(
    activeTheme: CyberTheme = CyberTheme.CYBERPUNK_NEON,
    content: @Composable () -> Unit
) {
    val colorScheme = getCyberColorScheme(activeTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
