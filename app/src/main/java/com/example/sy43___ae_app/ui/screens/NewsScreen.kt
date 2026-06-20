package com.example.sy43___ae_app.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sy43___ae_app.Back.DataBase.dataBaseManager
import com.example.sy43___ae_app.Back.FrontDTO.NewUI
import com.example.sy43___ae_app.ui.utils.LocationHelper
import com.example.sy43___ae_app.ui.utils.MarkdownText
import com.example.sy43___ae_app.ui.utils.UrlImage
import com.example.sy43___ae_app.ui.utils.formatBadgeDay
import com.example.sy43___ae_app.ui.utils.formatBadgeMonth
import com.example.sy43___ae_app.ui.utils.formatBadgeWeekday
import com.example.sy43___ae_app.ui.utils.formatJumpDate
import com.example.sy43___ae_app.ui.utils.formatNewsDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

/**
 * NewsScreen - Displays all news events grouped by date with a date selector
 */
@Composable
fun NewsScreen(modifier: Modifier = Modifier, db: dataBaseManager?) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    var currentLocation by remember { mutableStateOf<android.location.Location?>(null) }
    var news by remember { mutableStateOf<List<NewUI>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var hasLoaded by remember { mutableStateOf(false) }

    // Fetch location
    LaunchedEffect(Unit) {
        locationHelper.getCurrentLocation { location ->
            currentLocation = location
        }
    }

    // Load all news from database on initialization
    LaunchedEffect(db) {
        if (db == null) {
            hasLoaded = false
            return@LaunchedEffect
        }

        try {
            val result = withContext(Dispatchers.IO) {
                db.repository.getAllNews()
            }
            news = result.sortedBy { it.startDate }
            errorMessage = ""
        } catch (e: Exception) {
            Log.e("DB_DEBUG", "Erreur dans le LaunchedEffect", e)
            errorMessage = e.localizedMessage ?: "Erreur inconnue"
            news = emptyList()
        } finally {
            hasLoaded = true
        }
    }

    when {
        // Loading state
        db == null && !hasLoaded -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Chargement des nouvelles...", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Error state
        errorMessage.isNotBlank() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Empty state
        news.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Aucune nouvelle en base", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Loaded state - display news
        else -> {
            // Group news by date for display
            val groupedNews = news
                .groupBy { it.startDate.toLocalDate() }
                .toSortedMap()
                .entries
                .toList()

            var expanded by remember { mutableStateOf(false) }
            var selectedDate by remember(groupedNews) { mutableStateOf(groupedNews.first().key) }
            val listState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(groupedNews) {
                if (groupedNews.none { it.key == selectedDate }) {
                    selectedDate = groupedNews.first().key
                }
            }

            Column(modifier = modifier.fillMaxSize()) {
                // Date selector dropdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Aller au ${formatJumpDate(selectedDate)}")
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Choisir une date"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.92f),
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ) {
                        groupedNews.forEachIndexed { index, dayGroup ->
                            DropdownMenuItem(
                                text = { Text(formatJumpDate(dayGroup.key)) },
                                colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.onTertiary),
                                onClick = {
                                    selectedDate = dayGroup.key
                                    expanded = false
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            )
                        }
                    }
                }

                // Scrollable list of news grouped by date
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = groupedNews,
                        key = { _, dayGroup -> dayGroup.key.toString() }
                    ) { _, dayGroup ->
                        NewsDayCard(
                            dayNews = dayGroup.value,
                            currentLocation = currentLocation,
                            locationHelper = locationHelper,
                            onFollowClick = { newsId, followed ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    db?.repository?.toggleFollowNews(newsId, followed, db.notificationManager)
                                    val updatedNews = db?.repository?.getAllNews()?.sortedBy { it.startDate } ?: emptyList()
                                    withContext(Dispatchers.Main) {
                                        news = updatedNews
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * NewsDayCard - A card showing all news for a single day
 */
@Composable
private fun NewsDayCard(
    dayNews: List<NewUI>,
    currentLocation: android.location.Location?,
    locationHelper: LocationHelper,
    onFollowClick: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (dayNews.isEmpty()) return

    val sortedDayNews = remember(dayNews) { dayNews.sortedBy { it.startDate } }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            // Left side: Date badge
            DateBadge(
                startDate = sortedDayNews.first().startDate,
                modifier = Modifier.fillMaxHeight()
            )

            // Right side: Event details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                sortedDayNews.forEachIndexed { index, news ->
                    NewsEventContent(
                        news = news,
                        currentLocation = currentLocation,
                        locationHelper = locationHelper,
                        onFollowClick = { onFollowClick(news.id, !news.isFollowed) }
                    )
                    if (index < sortedDayNews.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    }
                }
            }
        }
    }
}

/**
 * NewsEventContent - Details of a single news event
 */
@Composable
fun NewsEventContent(
    news: NewUI,
    currentLocation: android.location.Location?,
    locationHelper: LocationHelper,
    onFollowClick: () -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Club logo
            UrlImage(
                url = news.logoUrl,
                contentDescription = "Logo de ${news.clubName}",
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Event title
                MarkdownText(
                    text = news.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                // Club name
                Text(
                    text = news.clubName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            IconButton(onClick = onFollowClick) {
                Icon(
                    imageVector = if (news.isFollowed) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = if (news.isFollowed) "Unfollow" else "Follow",
                    tint = if (news.isFollowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date range
            Text(
                text = "${formatNewsDate(news.startDate)} - ${formatNewsDate(news.endDate)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            // Distance
            if (currentLocation != null && news.latitude != null && news.longitude != null) {
                val distance = locationHelper.calculateDistance(
                    currentLocation.latitude, currentLocation.longitude,
                    news.latitude, news.longitude
                )
                Text(
                    text = locationHelper.formatDistance(distance),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Summary
        MarkdownText(
            text = news.summary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Navigate button
        if (news.latitude != null && news.longitude != null) {
            androidx.compose.material3.TextButton(
                onClick = {
                    val gmmIntentUri = Uri.parse("google.navigation:q=${news.latitude},${news.longitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("S'y rendre")
            }
        }
    }
}

/**
 * DateBadge - Colored badge showing weekday, day, and month
 */
@Composable
fun DateBadge(startDate: LocalDateTime, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .width(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = formatBadgeWeekday(startDate),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = formatBadgeDay(startDate),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = formatBadgeMonth(startDate),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
