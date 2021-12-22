package de.david072.schoolplanner.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.david072.schoolplanner.R
import de.david072.schoolplanner.database.entities.Exam
import de.david072.schoolplanner.database.entities.StateTask
import de.david072.schoolplanner.database.entities.Subject
import de.david072.schoolplanner.database.entities.Task
import de.david072.schoolplanner.database.repositories.ExamRepository
import de.david072.schoolplanner.database.repositories.SubjectRepository
import de.david072.schoolplanner.database.repositories.TaskRepository
import de.david072.schoolplanner.ui.AppTopAppBar
import de.david072.schoolplanner.ui.HorizontalButton
import de.david072.schoolplanner.ui.HorizontalSpacer
import de.david072.schoolplanner.util.Utils
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ViewTaskScreen(navController: NavController?, taskId: Int, isExam: Boolean = false) {
    val context = LocalContext.current
    val viewModel = viewModel<ViewTaskScreenViewModel>()
    viewModel.setTaskId(taskId, isExam)

    val task = viewModel.task.collectAsState()
    val exam = viewModel.exam.collectAsState()
    val subject = viewModel.subject.collectAsState()

    Scaffold(topBar = {
        AppTopAppBar(navController, true, actions = {
            IconButton(onClick = {
                MaterialAlertDialogBuilder(context)
                    .setTitle(context.getString(R.string.delete_dialog_title))
                    .setMessage(context.getString(R.string.delete_dialog_message))
                    .setPositiveButton(context.getString(R.string.general_delete)) { dialog, _ ->
                        viewModel.deleteTask()
                        dialog.dismiss()
                        navController?.popBackStack()
                    }
                    .setNegativeButton(context.getString(R.string.delete_dialog_negative_button)) { dialog, _ -> dialog.cancel() }
                    .show()
            }) {
                Icon(Icons.Outlined.Delete, "")
            }

            IconButton(onClick = {
                navController?.navigate("edit_${if (!isExam) "task" else "test"}/${taskId}")
            }) {
                Icon(Icons.Outlined.Edit, "")
            }
        })
    }, floatingActionButton = {
        if (isExam) return@Scaffold

        // TODO: Animate the size change and icon?
        ExtendedFloatingActionButton(
            onClick = {
                if (task.value != null)
                    viewModel.setCompleted(!task.value!!.completed)
            },
            text = { Text(if (task.value == null || !task.value!!.completed) "Complete" else "Mark incomplete") },
            backgroundColor = MaterialTheme.colors.primary,
            icon = {
                Icon(Icons.Outlined.Done, "")
            })
    }) {
        Column(modifier = Modifier.padding(all = 10.dp)) {
            // TODO: Prevent the text field from being selectable
            TextField(
                value = when {
                    task.value != null -> task.value!!.title
                    exam.value != null -> exam.value!!.title
                    else -> ""
                },
                label = { Text(stringResource(R.string.add_task_title_label)) },
                readOnly = true,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp)
            )

            HorizontalButton(
                text = when {
                    task.value != null -> Utils.formattedDate(task.value!!.dueDate, context)
                    exam.value != null -> Utils.formattedDate(exam.value!!.dueDate, context)
                    else -> ""
                },
                icon = Icons.Outlined.Event,
            )
            HorizontalSpacer()

            val reminderText = run {
                if (task.value == null && exam.value == null) stringResource(R.string.add_task_reminder_selector)

                val (dueDate, reminder) = when {
                    task.value != null -> task.value!!.dueDate to task.value!!.reminder
                    exam.value != null -> exam.value!!.dueDate to exam.value!!.reminder
                    else -> return@run stringResource(R.string.add_task_reminder_selector)
                }

                val reminderIndex = Utils.getReminderIndex(dueDate, reminder)
                if (reminderIndex == -1)
                    reminder.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
                else stringArrayResource(R.array.reminder_choices)[reminderIndex]
            }

            HorizontalButton(
                text = reminderText,
                icon = Icons.Outlined.Notifications,
            )
            HorizontalSpacer()
            HorizontalButton(
                text = subject.value?.name ?: "",
                icon = Icons.Outlined.School,
            )

            // TODO: Prevent the text field from being selectable
            TextField(
                value = when {
                    task.value != null -> task.value!!.description ?: ""
                    exam.value != null -> exam.value!!.description ?: ""
                    else -> ""
                },
                readOnly = true,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp),
                label = { Text(stringResource(R.string.add_task_description_label)) },
            )
        }
    }
}

class ViewTaskScreenViewModel(application: Application) :
    AndroidViewModel(application) {
    private val _task: MutableStateFlow<StateTask?> = MutableStateFlow(null)
    private val _exam: MutableStateFlow<Exam?> = MutableStateFlow(null)
    val task: StateFlow<StateTask?> = _task
    val exam: StateFlow<Exam?> = _exam

    private val _subject: MutableStateFlow<Subject?> = MutableStateFlow(null)
    val subject: StateFlow<Subject?> = _subject

    fun setTaskId(taskId: Int, isExam: Boolean = false) {
        viewModelScope.launch {
            val flow =
                if (!isExam) TaskRepository(getApplication()).findById(taskId) else ExamRepository(
                    getApplication()
                ).findById(taskId)
            flow.collect { value: Any? ->
                // Task could be null if it has been deleted (deleteTask())
                if (value == null) {
                    _task.value = null
                    _exam.value = null
                    cancel()
                    return@collect
                }

                val subjectId: Int =
                    when (value) {
                        is Task -> {
                            _task.value = value.toStateTask()
                            value.subjectId
                        }
                        is Exam -> {
                            _exam.value = value
                            value.subjectId
                        }
                        else -> -1
                    }

                SubjectRepository(getApplication()).findById(subjectId).collect { subject ->
                    _subject.value = subject
                }
            }
        }
    }

    fun setCompleted(completed: Boolean) {
        if (task.value == null) return

        viewModelScope.launch {
            val task = task.value.apply { this!!.completed = completed }!!
            TaskRepository(getApplication()).update(task.toTask())
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            if (task.value != null)
                TaskRepository(getApplication()).delete(task.value!!.toTask())
            else if (exam.value != null)
                ExamRepository(getApplication()).delete(exam.value!!)
        }
    }
}
