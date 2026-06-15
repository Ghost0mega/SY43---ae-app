package com.example.sy43___ae_app.ui.utils

import android.content.Intent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.core.net.toUri

/**
 * MarkdownText - Composable that renders Markdown-like text with support for:
 * - Headers (# ## ### etc.)
 * - Bold (**text** or __text__)
 * - Italic (*text* or _text_)
 * - Code blocks (`text`)
 * - Links ([text](url))
 * - Lists (- or * prefix)
 *
 * Clicking on links opens them in a browser
 */
@Composable
fun MarkdownText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = style.color
) {
    val context = LocalContext.current
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val headerColor = MaterialTheme.colorScheme.secondary
    val headerSizes = listOf(
        MaterialTheme.typography.headlineMedium.fontSize,
        MaterialTheme.typography.titleLarge.fontSize,
        MaterialTheme.typography.titleMedium.fontSize,
        MaterialTheme.typography.titleSmall.fontSize,
        MaterialTheme.typography.bodyLarge.fontSize,
        MaterialTheme.typography.bodyMedium.fontSize
    )
    val annotatedText = remember(text, headerColor, headerSizes) {
        markdownToAnnotatedString(
            markdown = text,
            headerColor = headerColor,
            headerSizes = headerSizes
        )
    }

    Text(
        text = annotatedText,
        modifier = modifier.pointerInput(annotatedText) {
            // Handle link clicks
            detectTapGestures { position ->
                val layoutResult = textLayoutResult ?: return@detectTapGestures
                val offset = layoutResult.getOffsetForPosition(position)
                annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()
                    ?.let { annotation ->
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                            )
                        }
                    }
            }
        },
        style = style.copy(color = color),
        onTextLayout = { textLayoutResult = it }
    )
}

/**
 * Convert Markdown text to AnnotatedString for rich text display
 * Supports: headers, bold, italic, code, links, lists
 */
private fun markdownToAnnotatedString(
    markdown: String,
    headerColor: Color,
    headerSizes: List<TextUnit>
): AnnotatedString = buildAnnotatedString {
    val lines = markdown.replace("\r\n", "\n").split('\n')

    lines.forEachIndexed { index, line ->
        val trimmed = line.trimEnd()
        val header = parseHeaderLine(trimmed)

        if (header != null) {
            // Process header line
            val (level, content) = header
            pushStyle(
                SpanStyle(
                    fontSize = headerFontSize(level, headerSizes),
                    fontWeight = FontWeight.Bold,
                    color = headerColor
                )
            )
            appendInlineMarkdown(content)
            pop()
        } else {
            // Process regular line with potential list markers
            val content = when {
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    append("• ")
                    trimmed.drop(2)
                }
                else -> trimmed
            }

            appendInlineMarkdown(content)
        }

        if (index != lines.lastIndex) {
            append('\n')
        }
    }
}

/**
 * Parse and extract header level and content
 * Returns null if line is not a header
 * Example: "## Title" returns Pair(2, "Title")
 */
private fun parseHeaderLine(line: String): Pair<Int, String>? {
    val candidate = line.trimStart()
    if (!candidate.startsWith('#')) return null

    val level = candidate.takeWhile { it == '#' }.length
    if (level == 0) return null

    val content = candidate.drop(level).trimStart()
    if (content.isEmpty()) return null

    return level to content
}

/**
 * Get the appropriate font size for a header level
 */
private fun headerFontSize(level: Int, headerSizes: List<TextUnit>): TextUnit {
    if (headerSizes.isEmpty()) return TextUnit.Unspecified
    val index = (level - 1).coerceIn(0, headerSizes.lastIndex)
    return headerSizes[index]
}

/**
 * Process inline Markdown formatting within a line
 * Handles: **bold**, __bold__, *italic*, _italic_, `code`, [links](url)
 */
private fun AnnotatedString.Builder.appendInlineMarkdown(text: String) {
    var index = 0

    while (index < text.length) {
        when {
            // Bold with double asterisk **text**
            text.startsWith("**", index) -> {
                val end = text.indexOf("**", startIndex = index + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(index + 2, end))
                    pop()
                    index = end + 2
                } else {
                    append(text[index])
                    index++
                }
            }

            // Bold with double underscore __text__
            text.startsWith("__", index) -> {
                val end = text.indexOf("__", startIndex = index + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(index + 2, end))
                    pop()
                    index = end + 2
                } else {
                    append(text[index])
                    index++
                }
            }

            // Code block `text`
            text[index] == '`' -> {
                val end = text.indexOf('`', startIndex = index + 1)
                if (end != -1) {
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0x1A000000)
                        )
                    )
                    append(text.substring(index + 1, end))
                    pop()
                    index = end + 1
                } else {
                    append(text[index])
                    index++
                }
            }

            // Link [text](url)
            text.startsWith("[", index) -> {
                val closingBracket = text.indexOf(']', startIndex = index + 1)
                val openingParen = if (closingBracket != -1) text.indexOf('(', startIndex = closingBracket + 1) else -1
                val closingParen = if (openingParen != -1) text.indexOf(')', startIndex = openingParen + 1) else -1

                if (closingBracket != -1 && openingParen == closingBracket + 1 && closingParen != -1) {
                    val label = text.substring(index + 1, closingBracket)
                    val url = text.substring(openingParen + 1, closingParen)
                    pushStringAnnotation(tag = "URL", annotation = url)
                    pushStyle(
                        SpanStyle(
                            color = Color(0xFF1565C0),
                            textDecoration = TextDecoration.Underline
                        )
                    )
                    append(label)
                    pop()
                    pop()
                    index = closingParen + 1
                } else {
                    append(text[index])
                    index++
                }
            }

            // Italic with single asterisk *text*
            text[index] == '*' -> {
                val end = text.indexOf('*', startIndex = index + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(text.substring(index + 1, end))
                    pop()
                    index = end + 1
                } else {
                    append(text[index])
                    index++
                }
            }

            // Italic with single underscore _text_
            text[index] == '_' -> {
                val end = text.indexOf('_', startIndex = index + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(text.substring(index + 1, end))
                    pop()
                    index = end + 1
                } else {
                    append(text[index])
                    index++
                }
            }

            // Regular character
            else -> {
                append(text[index])
                index++
            }
        }
    }
}

