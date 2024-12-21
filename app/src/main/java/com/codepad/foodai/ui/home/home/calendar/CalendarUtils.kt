package com.codepad.foodai.ui.home.home.calendar

import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CalendarUtils {

    fun generateMonthDays(): List<List<Triple<Date, Int, String>>> {
        val weekdays =
            DateFormatSymbols(Locale.getDefault()).shortWeekdays.filter { it.isNotEmpty() }
                .map { it.first().toString() }
        val today = Calendar.getInstance()
        val monthDays = mutableListOf<Triple<Date, Int, String>>()

        for (offset in -26..0) {
            val date = Calendar.getInstance().apply {
                time = today.time
                add(Calendar.DAY_OF_YEAR, offset)
            }.time
            val day = SimpleDateFormat("d", Locale.getDefault()).format(date).toInt()
            val weekdayIndex =
                Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_WEEK) - 1
            monthDays.add(Triple(date, day, weekdays[weekdayIndex]))
        }

        val weeks = monthDays.chunked(7).toMutableList()

        if (weeks.isNotEmpty() && weeks.last().size == 6) {
            val futureDate = Calendar.getInstance().apply {
                time = today.time
                add(Calendar.DAY_OF_YEAR, 1)
            }.time
            val futureDay = SimpleDateFormat("d", Locale.getDefault()).format(futureDate).toInt()
            val futureWeekdayIndex =
                Calendar.getInstance().apply { time = futureDate }.get(Calendar.DAY_OF_WEEK) - 1
            weeks[weeks.size - 1] =
                weeks.last() + Triple(futureDate, futureDay, weekdays[futureWeekdayIndex])
        }

        return weeks
    }

    fun findCurrentDayPosition(weeks: List<List<Triple<Date, Int, String>>>): Pair<Int, Int>? {
        val today = Calendar.getInstance().time
        val todayFormatted = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(today)
        for ((weekIndex, week) in weeks.withIndex()) {
            for ((dayIndex, day) in week.withIndex()) {
                if (SimpleDateFormat(
                        "yyyyMMdd", Locale.getDefault()
                    ).format(day.first) == todayFormatted
                ) {
                    return Pair(weekIndex, dayIndex)
                }
            }
        }
        return null
    }
}