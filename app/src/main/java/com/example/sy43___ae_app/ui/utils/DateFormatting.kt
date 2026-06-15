package com.example.sy43___ae_app.ui.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * DateFormatting utilities - Helper functions for date/time formatting in French
 */

/** Formatter for news dates: "dd/MM/yyyy HH:mm" */
val newsDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

/**
 * Format a LocalDateTime for news display
 * Example: "15/06/2026 14:30"
 */
fun formatNewsDate(date: LocalDateTime): String = date.format(newsDateFormatter)

/**
 * Format a date in French (day month year)
 * Example: "4 juin 2026"
 */
fun formatDateFrench(date: LocalDate): String {
    val dayOfMonth = date.dayOfMonth
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.FRENCH)
    val month = date.format(monthFormatter)
    val year = date.year
    return "$dayOfMonth $month $year"
}

/**
 * Format a day name in French
 * Example: "jeudi", "vendredi"
 */
fun formatDayNameFrench(date: LocalDate): String {
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH)
    return date.format(dayFormatter)
}

/**
 * Format month and year in French (uppercase month)
 * Example: "JUIN 2026"
 */
fun formatMonthFrench(yearMonth: YearMonth): String {
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.FRENCH)
    val month = yearMonth.format(monthFormatter)
    return "${month.uppercase()} ${yearMonth.year}"
}

/**
 * Format badge weekday (shortened, uppercase)
 * Example: "JEU" from "jeudi" (removes dots)
 */
fun formatBadgeWeekday(date: LocalDateTime): String {
    val pattern = DateTimeFormatter.ofPattern("EEE", Locale.FRENCH)
    return date.format(pattern).replace(".", "").uppercase(Locale.FRENCH)
}

/**
 * Format badge day (2-digit day number)
 * Example: "04"
 */
fun formatBadgeDay(date: LocalDateTime): String =
    date.format(DateTimeFormatter.ofPattern("dd"))

/**
 * Format badge month (shortened, uppercase)
 * Example: "JUN" from "juin" (removes dots)
 */
fun formatBadgeMonth(date: LocalDateTime): String {
    val pattern = DateTimeFormatter.ofPattern("MMM", Locale.FRENCH)
    return date.format(pattern).replace(".", "").uppercase(Locale.FRENCH)
}

/**
 * Format a date for dropdown selector (day of week + date)
 * Example: "Jeudi 04 Juin 2026"
 */
fun formatJumpDate(date: LocalDate): String {
    val pattern = DateTimeFormatter.ofPattern("EEE dd MMM yyyy", Locale.FRENCH)
    return date.format(pattern)
        .replace(".", "")
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }
}

