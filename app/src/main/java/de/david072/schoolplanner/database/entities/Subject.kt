package de.david072.schoolplanner.database.entities

import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val name: String,
    val abbreviation: String,
    @ColumnInfo(name = "color") val colorValue: Int
) {
    fun color(): Color = Color(colorValue)
}