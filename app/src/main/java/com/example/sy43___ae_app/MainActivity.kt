package com.example.sy43___ae_app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.lifecycleScope
import com.example.sy43___ae_app.Back.DataBase.dataBaseManager
import com.example.sy43___ae_app.ui.theme.SY43aeappTheme
import com.example.sy43___ae_app.ui.screens.CalendarScreen
import com.example.sy43___ae_app.ui.screens.NewsScreen
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.tooling.preview.Preview

/**
 * BottomTab enum - Represents the different tabs in the bottom navigation bar
 * Used for navigation between major screens (News, Calendar, Clubs, Settings)
 */
private enum class BottomTab(val label: String, val icon: ImageVector) {
    NEWS("News", Icons.Filled.Newspaper),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth),
    CLUBS("Clubs", Icons.Filled.Groups),
    SETTINGS("Settings", Icons.Filled.Settings)
}

/**
 * MainActivity - Main entry point for the application
 *
 * Responsibilities:
 * - Initialize the database manager
 * - Set up the app theme
 * - Display the main navigation UI
 */
class MainActivity : ComponentActivity() {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database asynchronously
        lifecycleScope.launch {
            dataBaseManager.init(this@MainActivity)
        }

        // Enable edge-to-edge display and set content
        enableEdgeToEdge()
        setContent {
            SY43aeappTheme(dynamicColor = false) {
                AppWithBottomNav(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

/**
 * AppWithBottomNav - Main app layout with bottom navigation
 *
 * Structure:
 * - Top: Content area (News, Calendar, Clubs, or Settings)
 * - Bottom: Navigation bar with 4 tabs
 *
 * @param modifier Layout modifier
 */
@Composable
private fun AppWithBottomNav(modifier: Modifier = Modifier) {
    // Track currently selected tab
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(BottomTab.NEWS.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Bottom navigation bar with all tabs
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

        // Display the appropriate screen based on selected tab
        when (BottomTab.entries[selectedTabIndex]) {
            BottomTab.NEWS -> NewsScreen(modifier = contentModifier, dataBaseManager.instance)
            BottomTab.CALENDAR -> CalendarScreen(modifier = contentModifier, dataBaseManager.instance)
            BottomTab.CLUBS -> PlaceholderScreen(title = "Clubs", modifier = contentModifier)
            BottomTab.SETTINGS -> PlaceholderScreen(title = "Settings", modifier = contentModifier)
        }
    }
}

/**
 * PlaceholderScreen - Temporary placeholder for unimplemented screens (Clubs, Settings)
 *
 * @param title The title of the screen
 * @param modifier Layout modifier
 */
@Composable
private fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = "$title screen", style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * Preview function for development/testing in Android Studio
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SY43aeappTheme(dynamicColor = false) {
        AppWithBottomNav()
    }
}