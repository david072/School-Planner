package de.david072.schoolplanner.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo val title: String,
    @ColumnInfo(name = "due_date") val dueDate: LocalDate,
    @ColumnInfo val reminder: LocalDate,
    @ColumnInfo(name = "subject_id") var subjectId: Int,
    @ColumnInfo val description: String?,
    @ColumnInfo var completed: Boolean
)
