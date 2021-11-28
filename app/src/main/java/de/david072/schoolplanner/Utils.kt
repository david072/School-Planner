package de.david072.schoolplanner

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class Utils {
    companion object {
        fun getReminderIndex(dueDate: LocalDate, reminderStartDate: LocalDate): Int {
            return when (val difference =
                (dueDate.toEpochDay() - reminderStartDate.toEpochDay()).toInt()) {
                in 0..4 -> difference
                7 -> 5
                14 -> 6
                else -> -1
            }
        }

        fun formattedDate(date: LocalDate, context: Context): String {
            val epochDay = date.toEpochDay()
            val currentEpochDay = LocalDate.now().toEpochDay()

            // Minus current day of week - 1 => Monday of current week,
            // plus one week => Monday of next week
            val startOfNextWeek = LocalDate.now().apply {
                minusDays(dayOfWeek.value.toLong() - 1)
                plusWeeks(1)
            }

            return when {
                epochDay == currentEpochDay -> context.resources.getString(R.string.date_today)
                epochDay - 1 == currentEpochDay -> context.resources.getString(R.string.date_tomorrow)
                epochDay - 2 == currentEpochDay -> context.resources.getString(R.string.date_in_two_days)
                (date.isAfter(startOfNextWeek) || date.isEqual(startOfNextWeek)) && date.isBefore(
                    startOfNextWeek.plusDays(8)
                ) -> context.resources.getString(R.string.date_next_week)
                    .replace("%weekDay%", date.format(DateTimeFormatter.ofPattern("eeee")))
                else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
            }
        }

        fun formattedDate(epochDay: Long, context: Context): String =
            formattedDate(LocalDate.ofEpochDay(epochDay), context)
    }
}