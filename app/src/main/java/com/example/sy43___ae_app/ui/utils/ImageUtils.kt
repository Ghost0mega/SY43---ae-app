package com.example.sy43___ae_app.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.sy43___ae_app.R

/**
 * UrlImage - Displays an image from a URL with fallback handling
 *
 * Features:
 * - Loads images from URLs
 * - Shows placeholder while loading
 * - Shows fallback icon on error
 * - Normalizes URLs with different formats (http, https, //, /)
 *
 * @param url The image URL to load
 * @param contentDescription Accessibility description
 * @param modifier Layout modifier
 */
@Composable
fun UrlImage(
    modifier: Modifier = Modifier,
    url: String,
    contentDescription: String? = null
) {
    val fallbackPainter = painterResource(id = R.drawable.ic_launcher_foreground)

    AsyncImage(
        model = normalizeImageUrl(url),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        placeholder = fallbackPainter,
        error = fallbackPainter,
        fallback = fallbackPainter
    )
}

/**
 * Normalize image URLs to ensure they are valid
 *
 * Handles the following cases:
 * - "https://..." or "http://..." -> returns as-is
 * - "//..." -> prepends "https:"
 * - "/..." -> prepends "https://ae.utbm.fr"
 * - "null", "none", "n/a" -> returns null
 * - Empty strings -> returns null
 *
 * @param rawUrl The raw URL string
 * @return Normalized URL or null if invalid
 */
private fun normalizeImageUrl(rawUrl: String): String? {
    val url = rawUrl.trim()

    // Handle empty URLs
    if (url.isEmpty()) return null

    // Handle special values that indicate missing images
    if (url.equals("null", ignoreCase = true) ||
        url.equals("none", ignoreCase = true) ||
        url.equals("n/a", ignoreCase = true)
    ) {
        return null
    }

    return when {
        // Already has protocol
        url.startsWith("https://") || url.startsWith("http://") -> url
        // Protocol-relative URL
        url.startsWith("//") -> "https:$url"
        // Relative URL - assumes UTBM domain
        url.startsWith("/") -> "https://ae.utbm.fr$url"
        // Unrecognized format
        else -> null
    }
}

