package de.david072.schoolplanner.screens

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Parcel
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Event
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.david072.schoolplanner.R
import de.david072.schoolplanner.database.AppDatabase
import de.david072.schoolplanner.database.entities.Task
import de.david072.schoolplanner.ui.AppTopAppBar
import de.david072.schoolplanner.ui.HorizontalButton
import de.david072.schoolplanner.ui.HorizontalSpacer
import de.david072.schoolplanner.ui.theme.SchoolPlannerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@Composable
fun AddTaskScreen(navController: NavController?) {
    val context = LocalContext.current
    val viewModel = viewModel<AddTaskViewModel>()

    Scaffold(topBar = { AppTopAppBar(navController, true) }) {
        Box(modifier = Modifier.padding(PaddingValues(all = 10.dp))) {
            var title by remember { mutableStateOf("") }
            var titleIsError by remember { mutableStateOf(false) }
            var description by remember { mutableStateOf("") }

            var dueDate: LocalDate? by remember { mutableStateOf(null) }
            var dueDateIsError by remember { mutableStateOf(false) }
            var reminderIndex by remember { mutableStateOf(-2) }
            var reminderStartDate: LocalDate? by remember { mutableStateOf(null) }
            var reminderIsError by remember { mutableStateOf(false) }

            val subjectId = navController?.currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<Int>("subject_id")
                ?.observeAsState()
            var subjectIsError by remember { mutableStateOf(false) }

            fun validate(): Boolean {
                var valid = true

                if (title.trim().isEmpty()) {
                    titleIsError = true
                    valid = false
                }
                if (dueDate == null) {
                    dueDateIsError = true
                    valid = false
                }
                if (reminderIndex == -2) {
                    reminderIsError = true
                    valid = false
                }
                if (subjectId?.value == null) {
                    subjectIsError = true
                    valid = false
                }

                return valid
            }

            Column(modifier = Modifier.fillMaxHeight()) {
                TextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleIsError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 15.dp),
                    maxLines = 1,
                    label = { Text(stringResource(R.string.add_task_title_label)) },
                    isError = titleIsError
                )

                fun evalReminderStartDate() {
                    if (reminderIndex == -2) return

                    if (reminderIndex == 0) {
                        reminderStartDate = dueDate
                        return
                    }

                    val daysDifference = when (reminderIndex) {
                        in 1..4 -> reminderIndex
                        5 -> 7
                        6 -> 14
                        else -> -1
                    }.toLong()

                    reminderStartDate =
                        if (dueDate != null) dueDate!!.minusDays(daysDifference) else LocalDate.now()
                            .minusDays(daysDifference)

                    reminderIsError = false
                }

                HorizontalButton(
                    text = if (dueDate == null) stringResource(R.string.add_task_due_date_selector) else dueDate!!.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                    ),
                    icon = Icons.Outlined.Event,
                    isError = dueDateIsError
                ) {
                    pickDate(context) {
                        dueDate = it
                        dueDateIsError = false
                        evalReminderStartDate()
                    }
                }
                HorizontalSpacer()

                HorizontalButton(
                    text = if (reminderStartDate == null) {
                        stringResource(R.string.add_task_reminder_selector)
                    } else stringArrayResource(R.array.reminder_choices)[reminderIndex],
                    icon = Icons.Filled.Notifications,
                    isError = reminderIsError,
                ) {
                    pickReminder(context) {
                        reminderIndex = it
                        evalReminderStartDate()
                    }
                }
                HorizontalSpacer()

                var subjectText = stringResource(R.string.add_task_subject_selector)
                if (subjectId?.value != null) {
                    subjectIsError = false

                    val subjectQueryState = AppDatabase.instance(LocalContext.current).subjectDao()
                        .findById(subjectId.value!!).collectAsState(initial = null)
                    if (subjectQueryState.value != null) subjectText =
                        subjectQueryState.value!!.name
                }

                HorizontalButton(
                    text = subjectText,
                    icon = Icons.Filled.School,
                    isError = subjectIsError
                ) { navController?.navigate("subject_select_dialog") }

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                    label = { Text(stringResource(R.string.add_task_description_label)) },
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = {
                            if (!validate()) return@Button
                            Task(
                                title = title,
                                dueDate = dueDate!!,
                                reminder = reminderStartDate!!,
                                subjectId = subjectId!!.value!!,
                                description = description
                            ).let { viewModel.insert(it) }
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.add_task_button))
                    }
                }
            }
        }
    }
}

class AddTaskViewModel(application: Application) : AndroidViewModel(application) {
    fun insert(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.instance((getApplication() as Application).baseContext).taskDao()
                .insert(task)
        }
    }
}

private fun pickDate(context: Context, onDateSelected: (LocalDate) -> Unit) {
    MaterialDatePicker.Builder
        .datePicker()
        .setCalendarConstraints(
            CalendarConstraints.Builder()
                .setStart(LocalDate.now().month.value.toLong())
                .setValidator(object : CalendarConstraints.DateValidator {
                    override fun describeContents(): Int = 0
                    override fun writeToParcel(dest: Parcel?, flags: Int) {}

                    // Can only select tomorrow and days after that
                    override fun isValid(date: Long): Boolean =
                        date > Calendar.getInstance().timeInMillis
                }).build()
        )
        .build()
        .apply {
            addOnPositiveButtonClickListener { selection ->
                onDateSelected(
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(selection),
                        TimeZone.getDefault().toZoneId()
                    ).toLocalDate()
                )
            }
            show((context as FragmentActivity).supportFragmentManager, "date-picker")
        }
}

private fun pickReminder(context: Context, onSelected: (selectedIndex: Int) -> Unit) {
    MaterialAlertDialogBuilder(context)
        .setTitle("Pick reminder")
        .setItems(R.array.reminder_choices) { _, which -> onSelected(which) }
        .show()
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(locale = "de")
@Composable
private fun Preview() {
    SchoolPlannerTheme {
        AddTaskScreen(null)
    }
}