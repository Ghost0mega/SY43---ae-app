package com.example.sy43___ae_app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sy43___ae_app.Back.DataBase.dataBaseManager
import com.example.sy43___ae_app.Back.FrontDTO.NewUI
import com.example.sy43___ae_app.ui.utils.LocationHelper
import com.example.sy43___ae_app.ui.utils.formatDateFrench
import com.example.sy43___ae_app.ui.utils.formatDayNameFrench
import com.example.sy43___ae_app.ui.utils.formatMonthFrench
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * CalendarScreen - Displays a scrollable calendar with events grouped by date
 *
 * Features:
 * - Month navigation with previous/next buttons
 * - Events displayed in chronological order
 * - Shows distance to event if location is available
 * - Tertiary-colored header matching the news page design
 * - Shows loading, error, and empty states
 */
@Composable
fun CalendarScreen(modifier: Modifier = Modifier, db: dataBaseManager?) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    var currentLocation by remember { mutableStateOf<android.location.Location?>(null) }
    var news by remember { mutableStateOf<List<NewUI>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var hasLoaded by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // Fetch location
    LaunchedEffect(Unit) {
        locationHelper.getCurrentLocation { location ->
            currentLocation = location
        }
    }

    // Load events from database on initialization
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
            Log.e("CALENDAR_DEBUG", "Erreur dans le CalendarScreen", e)
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
                Text(text = "Chargement du calendrier...", style = MaterialTheme.typography.titleMedium)
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

        // Loaded state - display calendar
        else -> {
            Column(modifier = modifier.fillMaxSize()) {
                // Calendar header with month navigation
                CalendarHeader(
                    currentMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
                )

                // Calendar grid and events (scrollable)
                CalendarGridWithEvents(
                    month = currentMonth,
                    events = news,
                    currentLocation = currentLocation,
                    locationHelper = locationHelper,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * CalendarHeader - Navigation bar with month display
 * Uses tertiary color theme to match the news page
 */
@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiary)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous month button
            OutlinedButton(
                onClick = onPreviousMonth,
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("<", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onTertiary)
            }

            // Current month/year display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatMonthFrench(currentMonth),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onTertiary,
                    textAlign = TextAlign.Center
                )
            }

            // Next month button
            OutlinedButton(
                onClick = onNextMonth,
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(">", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onTertiary)
            }
        }
    }
}

/**
 * CalendarGridWithEvents - Vertical scrollable list of dates with their events
 * Groups and filters events by the selected month
 */
@Composable
private fun CalendarGridWithEvents(
    month: YearMonth,
    events: List<NewUI>,
    currentLocation: android.location.Location?,
    locationHelper: LocationHelper,
    modifier: Modifier = Modifier
) {
    // Group events by date and filter by current month
    val eventsByDate = remember(events, month) {
        events
            .filter { event ->
                val eventMonth = YearMonth.from(event.startDate)
                eventMonth == month
            }
            .groupBy { it.startDate.toLocalDate() }
            .toSortedMap()
    }

    // Extract sorted list of dates that have events
    val datesWithEvents = remember(eventsByDate) {
        eventsByDate.keys.sorted()
    }

    // Display dates and their events in a scrollable column
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(datesWithEvents.size) { index ->
            val date = datesWithEvents[index]
            val dayEvents = eventsByDate[date] ?: emptyList()
            CalendarDateSection(
                date = date,
                events = dayEvents,
                currentLocation = currentLocation,
                locationHelper = locationHelper
            )
        }
    }
}

/**
 * CalendarDateSection - A section representing one date with all its events
 * Shows date header and a list of events for that date
 */
@Composable
private fun CalendarDateSection(
    date: LocalDate,
    events: List<NewUI>,
    currentLocation: android.location.Location?,
    locationHelper: LocationHelper
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Date header with day name (e.g., "4 juin 2026" - "jeudi")
        DateHeader(date = date)

        // Events for this date
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val sortedEvents = events.sortedBy { it.startDate }
            sortedEvents.forEachIndexed { index, event ->
                EventRow(
                    event = event,
                    currentLocation = currentLocation,
                    locationHelper = locationHelper
                )
                // Add divider between events
                if (index < sortedEvents.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

/**
 * DateHeader - Header showing the date and day name
 * Example: "4 juin 2026" on the left, "jeudi" on the right
 */
@Composable
private fun DateHeader(date: LocalDate) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date (e.g., "4 juin 2026")
        Text(
            text = formatDateFrench(date),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
        )

        // Day name (e.g., "jeudi")
        Text(
            text = formatDayNameFrench(date),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

/**
 * EventRow - Single event display within a day section
 * Shows time range, bullet indicator, and event title
 */
@Composable
private fun EventRow(
    event: NewUI,
    currentLocation: android.location.Location?,
    locationHelper: LocationHelper
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time range (e.g., "14:30 - 18:30")
        Column(
            modifier = Modifier.width(100.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${event.startDate.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${event.endDate.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Bullet point indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50)
                )
        )

        // Event title
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Distance in calendar row
            if (currentLocation != null && event.latitude != null && event.longitude != null) {
                val distance = locationHelper.calculateDistance(
                    currentLocation.latitude, currentLocation.longitude,
                    event.latitude, event.longitude
                )
                Text(
                    text = locationHelper.formatDistance(distance),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


