package com.example.newsexplorer.util

import android.text.Spanned
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.HtmlCompat

/**
 * Remembers the AnnotatedString conversion for basic HTML text using HtmlCompat.
 * Handles basic tags like <b>, <i>, <u>. Links are not clickable by default here.
 * Re-runs conversion only if htmlText changes.
 */
@Composable
fun rememberHtmlAnnotatedString(htmlText: String?): AnnotatedString {
    val validHtmlText = htmlText ?: ""
    // Convert HTML to Spanned and then to AnnotatedString using HtmlCompat
    return remember(validHtmlText) {
        HtmlCompat.fromHtml(validHtmlText, HtmlCompat.FROM_HTML_MODE_COMPACT).toAnnotatedStringSimple()
    }
}

/**
 * Basic extension function to convert Android Spanned text to Compose AnnotatedString.
 * This version handles common style spans like bold, italic, underline.
 * It DOES NOT handle complex spans like UrlSpan (clickable links) or ParagraphStyles robustly.
 */
private fun Spanned.toAnnotatedStringSimple(): AnnotatedString = buildAnnotatedString {
    val spanned = this@toAnnotatedStringSimple
    append(spanned.toString()) // Append the plain text first

    // Apply styles for known Spanned types
    getSpans(0, length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)

        when (span) {
            is android.text.style.StyleSpan -> {
                when (span.style) {
                    android.graphics.Typeface.BOLD -> addStyle(SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), start, end)
                    android.graphics.Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), start, end)
                    android.graphics.Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), start, end)
                }
            }
            is android.text.style.UnderlineSpan -> {
                addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
            }
            is android.text.style.ForegroundColorSpan -> {
                addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
            }
            // Add more basic span handlers if needed (e.g., StrikethroughSpan)
            // NOTE: Does not handle clickable URLSpan or complex ParagraphStyles like BulletSpan well.
        }
    }
}


