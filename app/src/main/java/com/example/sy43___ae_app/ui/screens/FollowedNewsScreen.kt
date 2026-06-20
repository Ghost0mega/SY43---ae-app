package com.example.sy43___ae_app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sy43___ae_app.Back.DataBase.dataBaseManager
import com.example.sy43___ae_app.Back.FrontDTO.NewUI
import com.example.sy43___ae_app.ui.utils.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * FollowedNewsScreen - Displays news that the user has followed
 */
@Composable
fun FollowedNewsScreen(modifier: Modifier = Modifier, db: dataBaseManager?) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    var currentLocation by remember { mutableStateOf<android.location.Location?>(null) }
    var news by remember { mutableStateOf<List<NewUI>>(emptyList()) }
    var hasLoaded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch location
    LaunchedEffect(Unit) {
        locationHelper.getCurrentLocation { location ->
            currentLocation = location
        }
    }

    LaunchedEffect(db) {
        if (db == null) return@LaunchedEffect
        try {
            val result = withContext(Dispatchers.IO) {
                db.repository.getAllNews().filter { it.isFollowed }
            }
            news = result.sortedBy { it.startDate }
        } catch (e: Exception) {
            Log.e("DB_DEBUG", "Error loading followed news", e)
        } finally {
            hasLoaded = true
        }
    }

    if (news.isEmpty() && hasLoaded) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Aucune nouvelle suivie", style = MaterialTheme.typography.titleMedium)
        }
    } else {
        val groupedNews = news
            .groupBy { it.startDate.toLocalDate() }
            .toSortedMap()
            .entries
            .toList()

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = groupedNews,
                key = { _, dayGroup -> dayGroup.key.toString() }
            ) { _, dayGroup ->
                FollowedNewsDayCard(
                    dayNews = dayGroup.value,
                    currentLocation = currentLocation,
                    locationHelper = locationHelper,
                    onFollowClick = { newsId, followed ->
                        coroutineScope.launch(Dispatchers.IO) {
                            db?.repository?.toggleFollowNews(newsId, followed, db.notificationManager)
                            val updatedNews = db?.repository?.getAllNews()?.filter { it.isFollowed }?.sortedBy { it.startDate } ?: emptyList()
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

@Composable
private fun FollowedNewsDayCard(
    dayNews: List<NewUI>,
    currentLocation: android.location.Location?,
    locationHelper: LocationHelper,
    onFollowClick: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
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
            DateBadge(
                startDate = dayNews.first().startDate,
                modifier = Modifier.fillMaxHeight()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                dayNews.forEachIndexed { index, news ->
                    NewsEventContent(
                        news = news,
                        currentLocation = currentLocation,
                        locationHelper = locationHelper,
                        onFollowClick = { onFollowClick(news.id, !news.isFollowed) }
                    )
                    if (index < dayNews.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    }
                }
            }
        }
    }
}
