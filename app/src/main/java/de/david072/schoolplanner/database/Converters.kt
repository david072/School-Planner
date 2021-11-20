package de.david072.schoolplanner.database

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    // region Color <=> Int (argb) (unused)
    @TypeConverter
    fun argbToColor(value: Int): Color = Color(value)

    @TypeConverter
    fun colorToArgb(color: Color): Int = color.toArgb()
    // endregion

    // region LocalDate <=> Long (epoch day)
    @TypeConverter
    fun epochDayToLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDay()
    // endregion

    // region Boolean <=> Int
    @TypeConverter
    fun intToBool(value: Int) = value == 1

    @TypeConverter
    fun boolToInt(value: Boolean) = if (value) 1 else 0
    // endregion
}