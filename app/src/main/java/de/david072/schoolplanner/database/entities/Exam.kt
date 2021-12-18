package de.david072.schoolplanner.database.entities

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.david072.schoolplanner.database.repositories.SubjectRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo val title: String,
    @ColumnInfo(name = "due_date") val dueDate: LocalDate,
    @ColumnInfo val reminder: LocalDate,
    @ColumnInfo(name = "subject_id") var subjectId: Int,
    @ColumnInfo val description: String?
) {
    suspend fun getSubject(context: Context) =
        SubjectRepository(context).findById(subjectId).first()
}
