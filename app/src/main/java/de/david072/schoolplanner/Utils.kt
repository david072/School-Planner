package de.david072.schoolplanner

import java.time.LocalDate

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
    }
}