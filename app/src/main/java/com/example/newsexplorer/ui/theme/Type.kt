package com.example.newsexplorer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Base font sizes for Medium setting
private val baseBodyLargeSize = 16.sp
private val baseBodyMediumSize = 14.sp
private val baseLabelMediumSize = 12.sp
private val baseTitleLargeSize = 22.sp
private val baseTitleMediumSize = 18.sp
private val baseHeadlineSmallSize = 24.sp

@Composable
@ReadOnlyComposable
fun appTypography(fontSize: FontSize = LocalFontSize.current): Typography {
    val multiplier = when (fontSize) {
        FontSize.Small -> 0.85f
        FontSize.Medium -> 1.0f
        FontSize.Large -> 1.15f
    }

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * multiplier).sp // Example adjustment
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * multiplier).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (baseHeadlineSmallSize.value * multiplier).sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (baseTitleLargeSize.value * multiplier).sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (baseTitleMediumSize.value * multiplier).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (baseBodyLargeSize.value * multiplier).sp,
            lineHeight = (baseBodyLargeSize.value * multiplier * 1.4f).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (baseBodyMediumSize.value * multiplier).sp,
            lineHeight = (baseBodyMediumSize.value * multiplier * 1.4f).sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (baseLabelMediumSize.value * multiplier).sp
        )
    )
}

val Typography = Typography(
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp),
)