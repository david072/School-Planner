package de.david072.schoolplanner.database.entities

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.david072.schoolplanner.database.repositories.SubjectRepository
import kotlinx.coroutines.flow.first
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
) {
    suspend fun getSubject(context: Context) =
        SubjectRepository(context).findById(subjectId).first()

    fun toStateTask() = StateTask(this)
}

// Helper class, so that the compose layout can react to changes made to a task's field
data class StateTask(
    private val _uid: MutableState<Int>,
    private val _title: MutableState<String>,
    private val _dueDate: MutableState<LocalDate>,
    private val _reminder: MutableState<LocalDate>,
    private var _subjectId: MutableState<Int>,
    private val _description: MutableState<String?>,
    private var _completed: MutableState<Boolean>
) {
    constructor(task: Task) : this(
        mutableStateOf(task.uid),
        mutableStateOf(task.title),
        mutableStateOf(task.dueDate),
        mutableStateOf(task.reminder),
        mutableStateOf(task.subjectId),
        mutableStateOf(task.description),
        mutableStateOf(task.completed)
    )

    var uid
        get() = _uid.value;
        set(value) {
            _uid.value = value
        }
    var title
        get() = _title.value;
        set(value) {
            _title.value = value
        }
    var dueDate
        get() = _dueDate.value;
        set(value) {
            _dueDate.value = value
        }
    var reminder
        get() = _reminder.value;
        set(value) {
            _reminder.value = value
        }
    var subjectId
        get() = _subjectId.value;
        set(value) {
            _subjectId.value = value
        }
    var description
        get() = _description.value;
        set(value) {
            _description.value = value
        }
    var completed
        get() = _completed.value;
        set(value) {
            _completed.value = value
        }

    fun toTask() = Task(uid, title, dueDate, reminder, subjectId, description, completed)
}
