package com.example.sy43___ae_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.sy43___ae_app.Back.DataBase.dataBaseManager
import com.example.sy43___ae_app.ui.screens.CalendarScreen
import com.example.sy43___ae_app.ui.screens.ClubsScreen
import com.example.sy43___ae_app.ui.screens.FollowedNewsScreen
import com.example.sy43___ae_app.ui.screens.NewsScreen
import com.example.sy43___ae_app.ui.theme.SY43aeappTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * BottomTab enum - Represents the different tabs in the bottom navigation bar
 * Used for navigation between major screens (News, Calendar, Clubs, Followed)
 */
private enum class BottomTab(val label: String, val icon: ImageVector) {
    NEWS("News", Icons.Filled.Newspaper),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth),
    CLUBS("Clubs", Icons.Filled.Groups),
    FOLLOWED("Followed", Icons.Filled.Star)
}

/**
 * MainActivity - Main entry point for the application
 */
class MainActivity : ComponentActivity() {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions
        val permissionsToRequest = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 101)
        }

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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppWithBottomNav(modifier: Modifier = Modifier) {
    // Track currently selected tab using PagerState to allow swiping
    val pagerState = rememberPagerState(initialPage = BottomTab.NEWS.ordinal) {
        BottomTab.entries.size
    }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Portail AE UTBM", style = MaterialTheme.typography.titleMedium)
                        Text("Étudiant : Ghost0mega", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Profil",
                        modifier = Modifier.padding(end = 16.dp).size(32.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            // Bottom navigation bar with all tabs
            NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                BottomTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
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

        // Use HorizontalPager to allow swiping between major screens
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1 // Keep adjacent pages loaded for smoother transition
        ) { pageIndex ->
            val tab = BottomTab.entries[pageIndex]
            Box(modifier = contentModifier) {
                when (tab) {
                    BottomTab.NEWS -> NewsScreen(db = dataBaseManager.instance)
                    BottomTab.CALENDAR -> CalendarScreen(db = dataBaseManager.instance)
                    BottomTab.CLUBS -> ClubsScreen(db = dataBaseManager.instance)
                    BottomTab.FOLLOWED -> FollowedNewsScreen(db = dataBaseManager.instance)
                }
            }
        }
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
