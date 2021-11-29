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

        fun formattedDate(
            date: LocalDate,
            context: Context,
            withPreposition: Boolean = false
        ): String {
            val epochDay = date.toEpochDay()
            val currentEpochDay = LocalDate.now().toEpochDay()

            // Minus current day of week - 1 => Monday of current week,
            // plus one week => Monday of next week
            val startOfNextWeek = LocalDate.now().apply {
                minusDays(dayOfWeek.value.toLong() - 1)
                plusWeeks(1)
            }

            return when {
                epochDay == currentEpochDay -> context.getString(
                    if (!withPreposition) R.string.date_today
                    else R.string.date_today_preposition
                )
                epochDay - 1 == currentEpochDay -> context.getString(
                    if (!withPreposition) R.string.date_tomorrow
                    else R.string.date_tomorrow_preposition
                )
                epochDay - 2 == currentEpochDay -> context.getString(
                    if (!withPreposition) R.string.date_in_two_days
                    else R.string.date_in_two_days_preposition
                )
                (date.isAfter(startOfNextWeek) || date.isEqual(startOfNextWeek)) && date.isBefore(
                    startOfNextWeek.plusDays(8)
                ) -> context.getString(
                    if (!withPreposition) R.string.date_next_week
                    else R.string.date_next_week_preposition
                ).replace("%weekDay%", date.format(DateTimeFormatter.ofPattern("eeee")))
                else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)).let {
                    if (!withPreposition) it
                    else context.getString(R.string.date_long_date_preposition)
                        .replace("%date%", it)
                }
            }
        }

        fun formattedDate(
            epochDay: Long,
            context: Context,
            withPreposition: Boolean = false
        ): String =
            formattedDate(LocalDate.ofEpochDay(epochDay), context, withPreposition)
    }
}