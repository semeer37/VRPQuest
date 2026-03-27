package com.lex.vrpquest.Utils

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

fun copyToClipboard(localClipboardManager:ClipboardManager, text: String) {
    localClipboardManager.setText(
        buildAnnotatedString {
            withStyle(
                SpanStyle(color = CustomColorScheme.onSurface)
            ) {
                append(text)
            }
        }
    )
}
