package it.superiaprivata.app.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Renders the lightweight Markdown subset commonly returned by the local model.
 * Supports bold, italic, headings and unordered list markers without adding a
 * network dependency or changing the stored conversation text.
 */
@Composable
fun MarkdownMessageText(
    markdown: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: androidx.compose.ui.text.style.TextOverflow = androidx.compose.ui.text.style.TextOverflow.Clip
) {
    val rendered = buildAnnotatedString {
        markdown.lines().forEachIndexed { lineIndex, rawLine ->
            if (lineIndex > 0) append("\n")
            val line = rawLine.trimStart()
            val bullet = line.startsWith("* ") || line.startsWith("- ")
            val heading = line.startsWith("#")
            val content = when {
                bullet -> "• " + line.drop(2)
                heading -> line.dropWhile { it == '#' }.trimStart()
                else -> rawLine
            }
            if (bullet) {
                withStyle(SpanStyle(fontWeight = FontWeight.Medium)) { append("• ") }
            }
            val body = if (bullet) line.drop(2) else content
            if (heading) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(body) }
            } else {
                val pattern = Regex("\\*\\*(.+?)\\*\\*|__(.+?)__|(?<!\\*)\\*([^*]+?)\\*")
                var cursor = 0
                pattern.findAll(body).forEach { match ->
                    append(body.substring(cursor, match.range.first))
                    val boldText = match.groups[1]?.value ?: match.groups[2]?.value
                    if (boldText != null) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(boldText) }
                    } else {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(match.groups[3]?.value.orEmpty())
                        }
                    }
                    cursor = match.range.last + 1
                }
                append(body.substring(cursor))
            }
        }
    }
    Text(
        text = rendered,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = maxLines,
        overflow = overflow
    )
}
