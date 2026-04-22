package com.example.sy43___ae_app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.sy43___ae_app.DataBase.FrontDTO.NewUI
import com.example.sy43___ae_app.DataBase.dataBaseManager
import com.example.sy43___ae_app.ui.theme.SY43aeappTheme
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private enum class BottomTab(val label: String, val icon: ImageVector) {
    NEWS("News", Icons.Filled.Home),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth),
    ClUBS("Clubs", Icons.Filled.Person),
    SETTINGS("Settings", Icons.Filled.Settings)
}

private val newsDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            dataBaseManager.init(this@MainActivity)
        }
        // UI
        enableEdgeToEdge()
        setContent {
            SY43aeappTheme(dynamicColor = false) {
                AppWithBottomNav(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun AppWithBottomNav(modifier: Modifier = Modifier) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(BottomTab.NEWS.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                BottomTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                            selectedTextColor = MaterialTheme.colorScheme.onSecondary,
                            indicatorColor = MaterialTheme.colorScheme.secondary,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f),
                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (BottomTab.entries[selectedTabIndex]) {
            BottomTab.NEWS -> NewsScreen(modifier = contentModifier, dataBaseManager.instance)
            BottomTab.CALENDAR -> PlaceholderScreen(title = "Calendar", modifier = contentModifier)
            BottomTab.ClUBS -> PlaceholderScreen(title = "Profile", modifier = contentModifier)
            BottomTab.SETTINGS -> PlaceholderScreen(title = "Settings", modifier = contentModifier)
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = "$title screen", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun NewsScreen(modifier: Modifier = Modifier, db: dataBaseManager?) {
    var news by remember { mutableStateOf<List<NewUI>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var hasLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(db) {
        if (db == null) {
            hasLoaded = false
            return@LaunchedEffect
        }

        try {
            val result = withContext(Dispatchers.IO) {
                db.repository.getAllNews()
            }
            news = result
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
        db == null && !hasLoaded -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Chargement des nouvelles...", style = MaterialTheme.typography.titleMedium)
            }
        }

        errorMessage.isNotBlank() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium)
            }
        }

        news.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Aucune nouvelle en base", style = MaterialTheme.typography.titleMedium)
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(news) { item ->
                    NewsCard(news = item)
                }
            }
        }
    }
}

@Composable
private fun NewsCard(news: NewUI, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UrlImage(
                    url = news.logoUrl,
                    contentDescription = "Logo de ${news.clubName}",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Column {
                    Text(
                        text = news.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = news.clubName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = news.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Du ${formatNewsDate(news.startDate)} au ${formatNewsDate(news.endDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatNewsDate(date: LocalDateTime): String = date.format(newsDateFormatter)

@Composable
fun UrlImage(modifier: Modifier = Modifier, url: String, contentDescription: String? = null) {
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SY43aeappTheme(dynamicColor = false) {
        AppWithBottomNav()
    }
}