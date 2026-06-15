package com.example.sy43___ae_app

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.sy43___ae_app.Back.FrontDTO.NewUI
import com.example.sy43___ae_app.Back.DataBase.dataBaseManager
import com.example.sy43___ae_app.ui.theme.SY43aeappTheme
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.LocalDate
import java.time.YearMonth

private enum class BottomTab(val label: String, val icon: ImageVector) {
    NEWS("News", Icons.Filled.Newspaper),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth),
    ClUBS("Clubs", Icons.Filled.Groups),
    SETTINGS("Settings", Icons.Filled.Settings)
}

private val newsDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

class MainActivity : ComponentActivity() {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
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
             BottomTab.CALENDAR -> CalendarScreen(modifier = contentModifier, dataBaseManager.instance)
             BottomTab.ClUBS -> PlaceholderScreen(title = "Clubs", modifier = contentModifier)
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
fun CalendarScreen(modifier: Modifier = Modifier, db: dataBaseManager?) {
    var news by remember { mutableStateOf<List<NewUI>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var hasLoaded by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

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
        db == null && !hasLoaded -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Chargement du calendrier...", style = MaterialTheme.typography.titleMedium)
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

        else -> {
            Column(modifier = modifier.fillMaxSize()) {
                // Calendar header with month navigation
                CalendarHeader(
                    currentMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
                )

                // Calendar grid and events
                CalendarGridWithEvents(
                    month = currentMonth,
                    events = news,
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
    modifier: Modifier = Modifier
) {
    // Create a map of date to events for quick lookup
    val eventsByDate = remember(events, month) {
        events
            .filter { event ->
                val eventMonth = YearMonth.from(event.startDate)
                eventMonth == month
            }
            .groupBy { it.startDate.toLocalDate() }
            .toSortedMap()
    }

    // Get all dates that have events in this month
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
            CalendarDateSection(date = date, events = dayEvents)
        }
    }
}

@Composable
private fun CalendarDateSection(date: LocalDate, events: List<NewUI>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Date header with day name
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
                EventRow(event = event)
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
        // Date (e.g., "4 juin 2026")
        Text(
            text = formatDateFrench(date),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )

        // Day name (e.g., "jeudi")
        Text(
            text = formatDayNameFrench(date),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun EventRow(event: NewUI) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time range
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

        // Bullet point
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50)
                )
        )

        // Event title
        Text(
            text = event.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatDateFrench(date: LocalDate): String {
    val dayOfMonth = date.dayOfMonth
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.FRENCH)
    val month = date.format(monthFormatter)
    val year = date.year
    return "$dayOfMonth $month $year"
}

private fun formatDayNameFrench(date: LocalDate): String {
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH)
    return date.format(dayFormatter)
}

private fun formatMonthFrench(yearMonth: YearMonth): String {
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.FRENCH)
    val month = yearMonth.format(monthFormatter)
    return "${month.uppercase()} ${yearMonth.year}"
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
                        NewsDayCard(dayNews = dayGroup.value)
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsDayCard(dayNews: List<NewUI>, modifier: Modifier = Modifier) {
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
            DateBadge(
                startDate = sortedDayNews.first().startDate,
                modifier = Modifier.fillMaxHeight()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                sortedDayNews.forEachIndexed { index, news ->
                    NewsEventContent(news = news)
                    if (index < sortedDayNews.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsEventContent(news: NewUI) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UrlImage(
                url = news.logoUrl,
                contentDescription = "Logo de ${news.clubName}",
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                MarkdownText(
                    text = news.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = news.clubName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Text(
            text = "${formatNewsDate(news.startDate)} - ${formatNewsDate(news.endDate)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        MarkdownText(
            text = news.summary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DateBadge(startDate: LocalDateTime, modifier: Modifier = Modifier) {
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

private fun formatNewsDate(date: LocalDateTime): String = date.format(newsDateFormatter)

private fun formatBadgeWeekday(date: LocalDateTime): String {
    val pattern = DateTimeFormatter.ofPattern("EEE", Locale.FRENCH)
    return date.format(pattern).replace(".", "").uppercase(Locale.FRENCH)
}

private fun formatBadgeDay(date: LocalDateTime): String = date.format(DateTimeFormatter.ofPattern("dd"))

private fun formatBadgeMonth(date: LocalDateTime): String {
    val pattern = DateTimeFormatter.ofPattern("MMM", Locale.FRENCH)
    return date.format(pattern).replace(".", "").uppercase(Locale.FRENCH)
}

private fun formatJumpDate(date: LocalDate): String {
    val pattern = DateTimeFormatter.ofPattern("EEE dd MMM yyyy", Locale.FRENCH)
    return date.format(pattern).replace(".", "").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }
}

@Composable
private fun MarkdownText(text: String, style: TextStyle, modifier: Modifier = Modifier, color: Color = style.color) {
    val context = LocalContext.current
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val headerColor = MaterialTheme.colorScheme.secondary
    val headerSizes = listOf(
        MaterialTheme.typography.headlineMedium.fontSize,
        MaterialTheme.typography.titleLarge.fontSize,
        MaterialTheme.typography.titleMedium.fontSize,
        MaterialTheme.typography.titleSmall.fontSize,
        MaterialTheme.typography.bodyLarge.fontSize,
        MaterialTheme.typography.bodyMedium.fontSize
    )
    val annotatedText = remember(text, headerColor, headerSizes) {
        markdownToAnnotatedString(
            markdown = text,
            headerColor = headerColor,
            headerSizes = headerSizes
        )
    }

    Text(
        text = annotatedText,
        modifier = modifier.pointerInput(annotatedText) {
            detectTapGestures { position ->
                val layoutResult = textLayoutResult ?: return@detectTapGestures
                val offset = layoutResult.getOffsetForPosition(position)
                annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()
                    ?.let { annotation ->
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                            )
                        }
                    }
            }
        },
        style = style.copy(color = color),
        onTextLayout = { textLayoutResult = it }
    )
}

private fun markdownToAnnotatedString(markdown: String, headerColor: Color, headerSizes: List<TextUnit>): AnnotatedString = buildAnnotatedString {
    val lines = markdown.replace("\r\n", "\n").split('\n')

    lines.forEachIndexed { index, line ->
        val trimmed = line.trimEnd()
        val header = parseHeaderLine(trimmed)

        if (header != null) {
            val (level, content) = header
            pushStyle(
                SpanStyle(
                    fontSize = headerFontSize(level, headerSizes),
                    fontWeight = FontWeight.Bold,
                    color = headerColor
                )
            )
            appendInlineMarkdown(content)
            pop()
        } else {
            val content = when {
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    append("• ")
                    trimmed.drop(2)
                }

                else -> trimmed
            }

            appendInlineMarkdown(content)
        }

        if (index != lines.lastIndex) {
            append('\n')
        }
    }
}

private fun parseHeaderLine(line: String): Pair<Int, String>? {
    val candidate = line.trimStart()
    if (!candidate.startsWith('#')) return null

    val level = candidate.takeWhile { it == '#' }.length
    if (level == 0) return null

    val content = candidate.drop(level).trimStart()
    if (content.isEmpty()) return null

    return level to content
}

private fun headerFontSize(level: Int, headerSizes: List<TextUnit>): TextUnit {
    if (headerSizes.isEmpty()) return TextUnit.Unspecified
    val index = (level - 1).coerceIn(0, headerSizes.lastIndex)
    return headerSizes[index]
}


private fun AnnotatedString.Builder.appendInlineMarkdown(text: String) {
    var index = 0

    while (index < text.length) {
        when {

            text.startsWith("**", index) -> {
                val end = text.indexOf("**", startIndex = index + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(index + 2, end))
                    pop()
                    index = end + 2
                } else {
                    append(text[index])
                    index++
                }
            }

            text.startsWith("__", index) -> {
                val end = text.indexOf("__", startIndex = index + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(index + 2, end))
                    pop()
                    index = end + 2
                } else {
                    append(text[index])
                    index++
                }
            }

            text[index] == '`' -> {
                val end = text.indexOf('`', startIndex = index + 1)
                if (end != -1) {
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0x1A000000)
                        )
                    )
                    append(text.substring(index + 1, end))
                    pop()
                    index = end + 1
                } else {
                    append(text[index])
                    index++
                }
            }

            text.startsWith("[", index) -> {
                val closingBracket = text.indexOf(']', startIndex = index + 1)
                val openingParen = if (closingBracket != -1) text.indexOf('(', startIndex = closingBracket + 1) else -1
                val closingParen = if (openingParen != -1) text.indexOf(')', startIndex = openingParen + 1) else -1

                if (closingBracket != -1 && openingParen == closingBracket + 1 && closingParen != -1) {
                    val label = text.substring(index + 1, closingBracket)
                    val url = text.substring(openingParen + 1, closingParen)
                    pushStringAnnotation(tag = "URL", annotation = url)
                    pushStyle(
                        SpanStyle(
                            color = Color(0xFF1565C0),
                            textDecoration = TextDecoration.Underline
                        )
                    )
                    append(label)
                    pop()
                    pop()
                    index = closingParen + 1
                } else {
                    append(text[index])
                    index++
                }
            }

            text[index] == '*' -> {
                val end = text.indexOf('*', startIndex = index + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(text.substring(index + 1, end))
                    pop()
                    index = end + 1
                } else {
                    append(text[index])
                    index++
                }
            }

            text[index] == '_' -> {
                val end = text.indexOf('_', startIndex = index + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(text.substring(index + 1, end))
                    pop()
                    index = end + 1
                } else {
                    append(text[index])
                    index++
                }
            }

            else -> {
                append(text[index])
                index++
            }
        }
    }
}

@Composable
fun UrlImage(modifier: Modifier = Modifier, url: String, contentDescription: String? = null) {
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

private fun normalizeImageUrl(rawUrl: String): String? {
    val url = rawUrl.trim()
    if (url.isEmpty()) return null
    if (url.equals("null", ignoreCase = true) || url.equals("none", ignoreCase = true) || url.equals("n/a", ignoreCase = true)) {
        return null
    }

    return when {
        url.startsWith("https://") || url.startsWith("http://") -> url
        url.startsWith("//") -> "https:$url"
        url.startsWith("/") -> "https://ae.utbm.fr$url"
        else -> null
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SY43aeappTheme(dynamicColor = false) {
        AppWithBottomNav()
    }
}