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

    LaunchedEffect(Unit) {
        locationHelper.getCurrentLocation { location ->
            currentLocation = location
        }
    }

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
            Log.e("CALENDAR_DEBUG", "Error in CalendarScreen", e)
            errorMessage = e.localizedMessage ?: "Erreur inconnue"
        } finally {
            hasLoaded = true
        }
    }

    when {
        db == null && !hasLoaded -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Chargement du calendrier...", style = MaterialTheme.typography.titleMedium)
            }
        }
        errorMessage.isNotBlank() -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium)
            }
        }
        else -> {
            Column(modifier = modifier.fillMaxSize()) {
                CalendarHeader(
                    currentMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
                )

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

            Text(
                text = formatMonthFrench(currentMonth),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onTertiary,
                textAlign = TextAlign.Center
            )

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

@Composable
private fun CalendarGridWithEvents(
    month: YearMonth,
    events: List<NewUI>,
    currentLocation: android.location.Location?,
    locationHelper: LocationHelper,
    modifier: Modifier = Modifier
) {
    val eventsByDate = remember(events, month) {
        events
            .filter { YearMonth.from(it.startDate) == month }
            .groupBy { it.startDate.toLocalDate() }
            .toSortedMap()
    }

    val datesWithEvents = remember(eventsByDate) {
        eventsByDate.keys.sorted()
    }

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
        DateHeader(date = date)

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
        Text(
            text = formatDateFrench(date),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
        )

        Text(
            text = formatDayNameFrench(date),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

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

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50)
                )
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
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
