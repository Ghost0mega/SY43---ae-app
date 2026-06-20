package com.example.sy43___ae_app

import com.example.sy43___ae_app.ui.utils.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class DateFormattingTest {

    @Test
    fun testFormatNewsDate() {
        val date = LocalDateTime.of(2026, 6, 15, 14, 30)
        assertEquals("15/06/2026 14:30", formatNewsDate(date))
    }

    @Test
    fun testFormatDateFrench() {
        val date = LocalDate.of(2026, 6, 4)
        assertEquals("4 juin 2026", formatDateFrench(date))
    }

    @Test
    fun testFormatDayNameFrench() {
        val date = LocalDate.of(2026, 6, 4) // It's a Thursday
        assertEquals("jeudi", formatDayNameFrench(date))
    }

    @Test
    fun testFormatMonthFrench() {
        val yearMonth = YearMonth.of(2026, 6)
        assertEquals("JUIN 2026", formatMonthFrench(yearMonth))
    }

    @Test
    fun testBadgeFormatting() {
        val date = LocalDateTime.of(2026, 6, 4, 10, 0)
        assertEquals("JEU", formatBadgeWeekday(date))
        assertEquals("04", formatBadgeDay(date))
        assertEquals("JUIN", formatBadgeMonth(date))
    }

    @Test
    fun testFormatJumpDate() {
        val date = LocalDate.of(2026, 6, 4)
        assertEquals("Jeu 04 juin 2026", formatJumpDate(date))
    }
}
