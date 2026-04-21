package com.example.sy43___ae_app

import com.example.sy43___ae_app.DataBase.ApiServices.NetworkDTO.NewsDateResult
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.sy43___ae_app.DataBase.ApiServices.ApiServiceImpl
import com.example.sy43___ae_app.DataBase.ApiServices.Services.newService
import com.example.sy43___ae_app.DataBase.FrontDTO.NewUI
import com.example.sy43___ae_app.DataBase.dataBaseManager
import com.example.sy43___ae_app.ui.theme.SY43aeappTheme
import coil.compose.AsyncImage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation


import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private enum class BottomTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth),
    PROFILE("Profile", Icons.Filled.Person),
    SETTINGS("Settings", Icons.Filled.Settings)
}

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
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(BottomTab.HOME.ordinal) }

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
            BottomTab.HOME -> newsBlock(modifier = contentModifier, dataBaseManager.instance)
            BottomTab.CALENDAR -> PlaceholderScreen(title = "Calendar", modifier = contentModifier)
            BottomTab.PROFILE -> PlaceholderScreen(title = "Profile", modifier = contentModifier)
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

@SuppressLint("RememberReturnType")
@Composable
fun newsBlock(modifier: Modifier = Modifier, db: dataBaseManager?) {
    var news by remember { mutableStateOf<List<NewUI>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(db) {
        if (db == null) return@LaunchedEffect // On attend que la DB soit prête

        try {
            val result = withContext(Dispatchers.IO) {
                db.repository.getAllNews()
            }
            news = result
        } catch (e: Exception) {
            Log.e("DB_DEBUG", "Erreur dans le LaunchedEffect", e)
            errorMessage = e.localizedMessage ?: "Erreur inconnue"
        }
    }


    val colors = listOf(
        Triple("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary),
        Triple(
            "Secondary",
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.onSecondary
        ),
        Triple(
            "Tertiary",
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiary
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UrlImageTest(modifier = Modifier.size(160.dp))

        Text(
            text = "Résultat de l'API :",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = news.getOrNull(0)?.title ?: if (news.isEmpty()) "Aucune news en base" else errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
        Text(
            text = news.getOrNull(0)?.summary ?: if (news.isEmpty()) "Aucune news en base" else errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        colors.forEach { (label, bgColor, textColor) ->
            Box(
                modifier = Modifier
                    .size(width = 200.dp, height = 60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = textColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun UrlImageTest(modifier: Modifier = Modifier) {
    AsyncImage(
        model = "https://ae.utbm.fr/static/core/img/logo_no_text.f7b33b3ef4d8.png",
        contentDescription = "UTBM AE logo",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SY43aeappTheme(dynamicColor = false) {
        AppWithBottomNav()
    }
}