package de.david072.schoolplanner.database

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    // Colors
    @TypeConverter
    fun argbToColor(value: Int): Color = Color(value)

    @TypeConverter
    fun colorToArgb(color: Color): Int = color.toArgb()

    // Dates
    @TypeConverter
    fun epochDayToLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDay()
}