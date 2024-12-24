package com.codepad.foodai.extensions

import android.annotation.SuppressLint
import com.codepad.foodai.helpers.LocaleHelper
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/*
*   Provides time duration as formatted string, e.g: 8h 25m.
*/
fun Pair<Long, Long>.getDurationAsString(): String {
    val difference = second - first
    val seconds = difference / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return String.format("%d h %d m", hours, minutes % 60)
}

/*
*   Provides minutes as formatted string, e.g: 8h 25m.
*/
fun Int.getDurationAsString(): String {
    val hours = this / 60
    val remainingMinutes = this % 60
    return String.format("%d h %d min", hours, remainingMinutes)
}

/*
*   Provide total duration times as minutes.
*   E.g: duration[0] = 00.23 - 00.33 duration[1] = 05.23 - 05.33
*   Total time as minute = 10 min + 10 min = 20 min
*/
fun List<Pair<Long, Long>>.getTotalTimeAsMinutes(): Int {
    var totalMinutes = 0
    forEachIndexed { _, pair ->
        val difference = pair.second - pair.first
        val seconds = (difference / 1000).toInt()
        totalMinutes += seconds / 60
    }
    return totalMinutes
}

/*
*   Provides -n days prior to the given date.
*/
@SuppressLint("SimpleDateFormat")
fun Date.getPriorDays(priorDayAmountToCollect: Int): List<Int> {
    val calendar = Calendar.getInstance()

    val requiredDays = mutableListOf<Int>()
    for (priorDay in 0 until priorDayAmountToCollect) {
        calendar.time = this
        calendar.add(Calendar.DAY_OF_YEAR, -(priorDay))
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        requiredDays.add(dayOfYear)
    }
    return requiredDays.reversed().toList()
}

/*
*   Provides -n days prior to the given date with
*   timeMillis pairs. (E.g: [[122332112,12323377],.,..])
*/
@SuppressLint("SimpleDateFormat")
fun Date.getPriorDaysWithTimeMillisPairs(
    priorDayAmountToCollect: Int,
    startMonth: Int? = null,
    requireMonthLimit: Boolean = false,
    year: Int? = null,
    requireYearLimit: Boolean = false
): List<Pair<Long, Long>> {
    val today = Calendar.getInstance()
    val tempCalendar = Calendar.getInstance()
    val dayTimeMillisPairList = mutableListOf<Pair<Long, Long>>()
    val singleDay = 1
    tempCalendar.time = this

    if (priorDayAmountToCollect == singleDay) {
        val startTime: Long
        val endTime: Long
        val isToday = tempCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

        if (isToday) {
            endTime = tempCalendar.time.time
            tempCalendar.resetDayToStart()
            startTime = tempCalendar.time.time
        } else {
            tempCalendar.resetDayToEnd()
            endTime = tempCalendar.time.time
            tempCalendar.resetDayToStart()
            startTime = tempCalendar.time.time
        }
        dayTimeMillisPairList.add(Pair(startTime, endTime))

    } else {
        if (year != null) {
            val isCurrentYear = year == today.get(Calendar.YEAR)
            if (isCurrentYear) {
                val yearEndDayEnd = tempCalendar.time.time
                tempCalendar.resetDayToStart()
                val yearEndDayStart = tempCalendar.time.time
                dayTimeMillisPairList.add(Pair(yearEndDayStart, yearEndDayEnd))
            } else {
                tempCalendar.resetYearToEnd()
                val yearEndDayEnd = tempCalendar.time.time
                tempCalendar.resetDayToStart()
                val yearEndDayStart = tempCalendar.time.time
                dayTimeMillisPairList.add(Pair(yearEndDayStart, yearEndDayEnd))
            }
        } else {
            val currentDayEnd = tempCalendar.time.time
            tempCalendar.resetDayToStart()
            val currentDayStart = tempCalendar.time.time
            dayTimeMillisPairList.add(Pair(currentDayStart, currentDayEnd))
        }

        for (priorDay in 1 until (priorDayAmountToCollect)) {
            val previousDay = dayTimeMillisPairList[priorDay - 1].second
            tempCalendar.time.time = previousDay

            val endTime: Long = tempCalendar.time.time
            tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
            tempCalendar.resetDayToStart()
            val startTime: Long = tempCalendar.time.time
            dayTimeMillisPairList.add(Pair(startTime, endTime))
        }
    }

    /*
    *   Filter day values to make sure added days
    *   are in same month (depends on requireMonthLimit)
    */
    if (requireMonthLimit) {
        val filterCalendar = Calendar.getInstance()
        val list = dayTimeMillisPairList.filter {
            filterCalendar.time = Date(it.first)
            val isInSameMonth = startMonth == filterCalendar.get(Calendar.MONTH)
            isInSameMonth
        }
        return list.reversed().toList()
    }

    /*
   *   Filter day values of year to make sure added days
   *   are in same year (depends on requireYearLimit)
   */
    if (requireYearLimit) {
        val filterCalendar = Calendar.getInstance()
        val list = dayTimeMillisPairList.filter {
            filterCalendar.time = Date(it.first)
            val isInSameYear = year == filterCalendar.get(Calendar.YEAR)
            isInSameYear
        }
        return list.reversed().toList()
    }

    return dayTimeMillisPairList.reversed().toList()
}

/*
*   Provides the day of the month
*/
fun Date.getDayOfYear(newDayStartTime: Double = 24.00): Int {
    val calendar = Calendar.getInstance()
    calendar.time = this
    val hourOfDate = this.time.getHourFormat()
    val day = calendar.get(Calendar.DAY_OF_YEAR)
    return if (hourOfDate in newDayStartTime..24.00) {
        day + 1
    } else {
        day
    }
}

/*
*   Provides the hour in 24 format for a given time.
*   E.g: 16.23, 24.22, 11.2, 6.22.
*/
@SuppressLint("SimpleDateFormat")
fun Long.getHourFormat(): Double {
    val hourFormat: DateFormat = SimpleDateFormat("HH")
    val minutesFormat: DateFormat = SimpleDateFormat("mm")
    var hour = hourFormat.format(Date(this)).toDouble()
    val minutes = minutesFormat.format(Date(this)).toDouble() % 60

    (if (hour == .0) {
        24.0
    } else {
        hour
    }).also { hour = it }

    return hour + (minutes * .01)
}

fun Long.getHourValue(): Int {
    val hourFormat = this.getHourFormat().toInt()
    return if (hourFormat == 24) 0 else hourFormat
}

/*
*   Provides name of day for a date - e.g: Thursday
*/
fun Date.getDayDisplayName(): String {
    val calendar = Calendar.getInstance()
    calendar.time = this
    val dayName = calendar.getDisplayName(
        Calendar.DAY_OF_WEEK,
        Calendar.LONG,
        LocaleHelper.provideFormattingLocale()
    )
    return dayName.orEmpty()
}

/*
*   Provides name of month for a date - e.g: April
*/
fun Calendar.getMonthDisplayName(characterLimit: Int = 3): String {
    return getDisplayName(
        Calendar.MONTH,
        Calendar.LONG,
        LocaleHelper.provideFormattingLocale()
    )?.take(characterLimit)
        .orEmpty()
}

/*
*   Resets current date to start of day
*   E.g: Date: 11.01.2022 22:33
*   Date After Reset: 11.01.2022 00:00
*/
fun Calendar.resetDayToStart() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
}

/*
*   Resets current date to end of day
*   E.g: Date: 11.01.2022 22:33
*   Date After Reset: 11.01.2022 23:59
*/
fun Calendar.resetDayToEnd() {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
}

/*
*   Resets current day to start of year
*/
fun Calendar.resetYearToStart() {
    set(Calendar.DAY_OF_YEAR, 1)
}

/*
*   Resets current day to end of year
*/
fun Calendar.resetYearToEnd() {
    set(Calendar.MONTH, 11)
    val daysOfMonth = this.getDaysOfMonth()
    set(Calendar.DAY_OF_MONTH, daysOfMonth)
}

/*
*   Provides day count of month of date
*/
fun Calendar.getDaysOfMonth(): Int {
    return this.getActualMaximum(Calendar.DAY_OF_MONTH)
}

/*
*   Provides day count of year of date
*/
fun Calendar.getDaysOfYear(): Int {
    return this.getActualMaximum(Calendar.DAY_OF_YEAR)
}

/*
*   Provides time value correctly in 0X format
*/
fun Int.asTwoDigitTime(): String {
    if (this < 10) {
        return "0$this"
    }
    return this.toString()
}

/*
*   Provides the day difference of
*   the given day with current day
*/
fun Date.dayDifference(): Long {
    val timeDifference = Date().time - time
    return TimeUnit.MILLISECONDS.toDays(timeDifference)
}

/*
*   Provides the hour value of
*   the given time at HH:mm format
*/
fun String.getHour(): Int {
    val sdf = SimpleDateFormat("HH:mm", LocaleHelper.provideFormattingLocale())
    val date = sdf.parse(this)
    val calendar = Calendar.getInstance().apply {
        time = date
    }
    return calendar.get(Calendar.HOUR)
}

/*
*   Provides the minute value of
*   the given time at HH:mm format
*/
fun String.getMinute(): Int {
    val sdf = SimpleDateFormat("HH:mm", LocaleHelper.provideFormattingLocale())
    val date = sdf.parse(this)
    val calendar = Calendar.getInstance().apply {
        time = date
    }
    return calendar.get(Calendar.MINUTE)
}

fun Date.toDayMonthYearFormat(): String {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", LocaleHelper.provideFormattingLocale())
    return dateFormat.format(this)
}

fun Date?.getDateYear(): Int? {
    return if (this == null) return null else Calendar.getInstance()
        .apply { time = this@getDateYear }.get(Calendar.YEAR)
}

fun provideMonthsWithFirstLetter(): List<String> {
    val dateFormat = SimpleDateFormat("MMM", LocaleHelper.provideFormattingLocale())
    val monthNames = List(12) { index ->
        val calendar = Calendar.getInstance()
        calendar[Calendar.MONTH] = index
        dateFormat.format(calendar.time).substring(0, 1)
    }
    return monthNames
}

fun getFormattedDate(date: Date?): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    return dateFormat.format(date ?: Date())
}


fun Date.toHourString(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(this)
}