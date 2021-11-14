package de.david072.schoolplanner.screens

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.david072.schoolplanner.R
import de.david072.schoolplanner.database.AppDatabase
import de.david072.schoolplanner.database.entities.Subject
import de.david072.schoolplanner.database.entities.Task
import de.david072.schoolplanner.ui.AppTopAppBar
import de.david072.schoolplanner.ui.HorizontalButton
import de.david072.schoolplanner.ui.HorizontalSpacer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ViewTaskScreen(navController: NavController?, taskId: Int) {
    val viewModel = viewModel<ViewTaskScreenViewModel>()
    viewModel.setTaskId(taskId)

    val task = viewModel.task.collectAsState()
    val subject = viewModel.subject.collectAsState()

    Scaffold(topBar = { AppTopAppBar(navController, true) }) {
        Column(modifier = Modifier.padding(all = 10.dp)) {
            // TODO: Prevent the text field from being selectable
            TextField(
                value = task.value?.title ?: "",
                label = { Text(stringResource(R.string.add_task_title_label)) },
                readOnly = true,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp)
            )

            HorizontalButton(
                text = task.value?.dueDate?.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                ) ?: "",
                icon = Icons.Outlined.Event,
            )
            HorizontalSpacer()
            HorizontalButton(
                text = task.value?.reminder?.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                ) ?: "",
                icon = Icons.Outlined.Notifications,
            )
            HorizontalSpacer()
            HorizontalButton(
                text = subject.value?.name ?: "",
                icon = Icons.Outlined.School,
            )

            // TODO: Prevent the text field from being selectable
            TextField(
                value = task.value?.description ?: "",
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
    private val _task: MutableStateFlow<Task?> = MutableStateFlow(null)
    val task: StateFlow<Task?> = _task

    private val _subject: MutableStateFlow<Subject?> = MutableStateFlow(null)
    val subject: StateFlow<Subject?> = _subject

    fun setTaskId(taskId: Int) {
        viewModelScope.launch {
            val appDatabase =
                AppDatabase.instance((getApplication() as Application).applicationContext)
            appDatabase.taskDao().findById(taskId).collect {
                _task.value = it
                appDatabase.subjectDao().findById(it.subjectId).collect { subject ->
                    _subject.value = subject
                }
            }
        }
    }
}

// Disabled, since the view task screen needs a subject id
/*@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(locale = "de")
@Composable
private fun Preview() {
    SchoolPlannerTheme {
        ViewTaskScreen(null, -1)
    }
}*/