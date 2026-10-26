package com.example.ui.terminal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * ANSI Escape Code Parser for Jetpack Compose.
 * Supports:
 * - SGR 0 (Reset)
 * - SGR 1 (Bold), 2 (Faint), 3 (Italic), 4 (Underline)
 * - Standard 16 colors (30-37, 90-97 for foreground; 40-47, 100-107 for background)
 * - 256 colors (\u001b[38;5;Nm, \u001b[48;5;Nm)
 * - 24-bit TrueColor RGB (\u001b[38;2;r;g;bm, \u001b[48;2;r;g;bm)
 */
object AnsiColorParser {

    private val ANSI_REGEX = Regex("\u001B\\[([0-9;]*)m")

    // Standard 16 ANSI Palette
    private val COLOR_PALETTE_16 = mapOf(
        0 to Color(0xFF000000), // Black
        1 to Color(0xFFE06C75), // Red
        2 to Color(0xFF98C379), // Green
        3 to Color(0xFFE5C07B), // Yellow
        4 to Color(0xFF61AFEF), // Blue
        5 to Color(0xFFC678DD), // Magenta
        6 to Color(0xFF56B6C2), // Cyan
        7 to Color(0xFFDCDFE4), // White
        // Bright variants
        8 to Color(0xFF5C6370), // Bright Black (Gray)
        9 to Color(0xFFFF5252), // Bright Red
        10 to Color(0xFF00FF66), // Bright Green
        11 to Color(0xFFFFD600), // Bright Yellow
        12 to Color(0xFF40C4FF), // Bright Blue
        13 to Color(0xFFFF4081), // Bright Magenta
        14 to Color(0xFF00E5FF), // Bright Cyan
        15 to Color(0xFFFFFFFF)  // Bright White
    )

    fun parse(rawText: String, defaultColor: Color): AnnotatedString {
        if (!rawText.contains("\u001B[")) {
            return AnnotatedString(rawText)
        }

        return buildAnnotatedString {
            var currentIndex = 0
            var currentFgColor: Color? = null
            var currentBgColor: Color? = null
            var isBold = false
            var isItalic = false
            var isUnderline = false

            val matches = ANSI_REGEX.findAll(rawText).toList()

            for (match in matches) {
                val matchStart = match.range.first
                val matchEnd = match.range.last + 1

                if (matchStart > currentIndex) {
                    val textChunk = rawText.substring(currentIndex, matchStart)
                    val chunkStartInAnnotated = length
                    append(textChunk)
                    val chunkEndInAnnotated = length

                    addStyle(
                        SpanStyle(
                            color = currentFgColor ?: defaultColor,
                            background = currentBgColor ?: Color.Transparent,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                            textDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None
                        ),
                        chunkStartInAnnotated,
                        chunkEndInAnnotated
                    )
                }

                // Process ANSI codes
                val codesString = match.groupValues[1]
                val codes = if (codesString.isEmpty()) {
                    listOf(0)
                } else {
                    codesString.split(";").mapNotNull { it.toIntOrNull() }
                }

                var i = 0
                while (i < codes.size) {
                    val code = codes[i]
                    when (code) {
                        0 -> { // Reset all
                            currentFgColor = null
                            currentBgColor = null
                            isBold = false
                            isItalic = false
                            isUnderline = false
                        }
                        1 -> isBold = true
                        2 -> isBold = false // Faint
                        3 -> isItalic = true
                        4 -> isUnderline = true
                        22 -> isBold = false
                        23 -> isItalic = false
                        24 -> isUnderline = false
                        // Standard Foregrounds 30-37
                        in 30..37 -> currentFgColor = COLOR_PALETTE_16[code - 30]
                        39 -> currentFgColor = null // Default FG
                        // Standard Backgrounds 40-47
                        in 40..47 -> currentBgColor = COLOR_PALETTE_16[code - 40]
                        49 -> currentBgColor = null // Default BG
                        // Bright Foregrounds 90-97
                        in 90..97 -> currentFgColor = COLOR_PALETTE_16[code - 90 + 8]
                        // Bright Backgrounds 100-107
                        in 100..107 -> currentBgColor = COLOR_PALETTE_16[code - 100 + 8]
                        // 256 or TrueColor Foregrounds
                        38 -> {
                            if (i + 1 < codes.size) {
                                when (codes[i + 1]) {
                                    5 -> { // 256 color
                                        if (i + 2 < codes.size) {
                                            currentFgColor = get256Color(codes[i + 2])
                                            i += 2
                                        }
                                    }
                                    2 -> { // TrueColor RGB
                                        if (i + 4 < codes.size) {
                                            currentFgColor = Color(codes[i + 2], codes[i + 3], codes[i + 4])
                                            i += 4
                                        }
                                    }
                                }
                            }
                        }
                        // 256 or TrueColor Backgrounds
                        48 -> {
                            if (i + 1 < codes.size) {
                                when (codes[i + 1]) {
                                    5 -> {
                                        if (i + 2 < codes.size) {
                                            currentBgColor = get256Color(codes[i + 2])
                                            i += 2
                                        }
                                    }
                                    2 -> {
                                        if (i + 4 < codes.size) {
                                            currentBgColor = Color(codes[i + 2], codes[i + 3], codes[i + 4])
                                            i += 4
                                        }
                                    }
                                }
                            }
                        }
                    }
                    i++
                }

                currentIndex = matchEnd
            }

            // Append remaining trailing text
            if (currentIndex < rawText.length) {
                val remaining = rawText.substring(currentIndex)
                val start = length
                append(remaining)
                addStyle(
                    SpanStyle(
                        color = currentFgColor ?: defaultColor,
                        background = currentBgColor ?: Color.Transparent,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None
                    ),
                    start,
                    length
                )
            }
        }
    }

    private fun get256Color(colorIndex: Int): Color {
        if (colorIndex in 0..15) {
            return COLOR_PALETTE_16[colorIndex] ?: Color.White
        }
        // 6x6x6 Color Cube (16..231)
        if (colorIndex in 16..231) {
            val cube = colorIndex - 16
            val r = (cube / 36) * 51
            val g = ((cube % 36) / 6) * 51
            val b = (cube % 6) * 51
            return Color(r, g, b)
        }
        // Grayscale ramp (232..255)
        if (colorIndex in 232..255) {
            val gray = (colorIndex - 232) * 10 + 8
            return Color(gray, gray, gray)
        }
        return Color.White
    }

    fun stripAnsi(text: String): String {
        return text.replace(ANSI_REGEX, "")
    }
}
