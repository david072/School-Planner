package de.david072.schoolplanner.screens

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Parcel
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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
import de.david072.schoolplanner.Utils
import de.david072.schoolplanner.database.AppDatabase
import de.david072.schoolplanner.database.entities.Task
import de.david072.schoolplanner.ui.AppTopAppBar
import de.david072.schoolplanner.ui.HorizontalButton
import de.david072.schoolplanner.ui.HorizontalSpacer
import de.david072.schoolplanner.ui.theme.AppColors
import de.david072.schoolplanner.ui.theme.SchoolPlannerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@Composable
fun AddTaskScreen(navController: NavController?, taskIdToEdit: Int? = null) {
    val context = LocalContext.current
    val viewModel = viewModel<AddTaskViewModel>()

    if (taskIdToEdit != null) viewModel.setTaskId(taskIdToEdit)
    val taskToEdit = viewModel.taskToEdit.collectAsState()

    var didSetValues by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var titleIsError by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    var dueDate: LocalDate? by remember { mutableStateOf(null) }
    var dueDateIsError by remember { mutableStateOf(false) }
    var reminderIndex by remember { mutableStateOf(-2) }
    var reminderStartDate: LocalDate? by remember { mutableStateOf(null) }

    val subjectId = navController?.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Int>("subject_id")
        // Modify the value the first time if we're editing
        ?.apply {
            if (taskToEdit.value != null && !didSetValues) value =
                taskToEdit.value!!.subjectId
        }
        ?.observeAsState()
    var subjectIsError by remember { mutableStateOf(false) }

    // region Helper functions (validate(), evalReminderStartDate())
    fun validate(): Boolean {
        var valid = true

        // TODO: Add validation for each task item
        if (taskIdToEdit != null) {
            if (title.trim().isEmpty()) {
                titleIsError = true
                valid = false
            }
        }
        if (dueDate == null) {
            dueDateIsError = true
            valid = false
        }
        if (reminderIndex == -2) valid = false
        if (subjectId?.value == null) {
            subjectIsError = true
            valid = false
        }

        return valid
    }

    fun evalReminderStartDate(index: Int = reminderIndex): LocalDate? {
        if (index == -2) return null

        if (index == 0) {
            reminderStartDate = dueDate
            return dueDate
        }

        val daysDifference = when (index) {
            in 0..4 -> index
            5 -> 7
            6 -> 14
            else -> -1
        }.toLong()

        reminderStartDate =
            if (dueDate != null) dueDate!!.minusDays(daysDifference) else LocalDate.now()
                .minusDays(daysDifference)

        return reminderStartDate
    }
    // endregion

    Scaffold(topBar = {
        AppTopAppBar(navController, true, actions = {
            if (taskIdToEdit == null) {
                IconButton(onClick = { viewModel.tasks.add(TaskData()) }) {
                    Icon(Icons.Filled.Add, "")
                }
            }
        })
    }, floatingActionButton = {
        ExtendedFloatingActionButton(onClick = {
            if (!validate()) return@ExtendedFloatingActionButton
            if (taskToEdit.value != null) {
                Task(
                    uid = taskToEdit.value!!.uid,
                    title = title,
                    dueDate = dueDate!!,
                    reminder = reminderStartDate!!,
                    subjectId = subjectId!!.value!!,
                    description = description,
                    completed = taskToEdit.value!!.completed
                ).let { viewModel.update(it) }
            } else {
                val tasks = arrayListOf<Task>()
                viewModel.tasks.forEach {
                    // Override reminder if a different one has been selected
                    val reminder =
                        if (it.reminderIndex == -2 ||
                            it.reminderIndex == reminderIndex
                        ) reminderStartDate!!
                        else evalReminderStartDate(it.reminderIndex)!!

                    tasks.add(
                        Task(
                            title = it.title,
                            dueDate = dueDate!!,
                            reminder = reminder,
                            subjectId = subjectId!!.value!!,
                            description = it.description,
                            completed = false
                        )
                    )
                }
                viewModel.insertAll(tasks)
            }
            navController?.popBackStack()
        }, icon = {
            Icon(
                if (taskToEdit.value == null) Icons.Filled.Add
                else Icons.Outlined.Save,
                ""
            )
        }, text = {
            Text(
                if (taskToEdit.value == null) stringResource(R.string.add_task_button)
                else stringResource(R.string.general_save)
            )
        }, backgroundColor = MaterialTheme.colors.primary)
    }) {
        Box(modifier = Modifier.padding(PaddingValues(all = 10.dp))) {
            val scrollState = rememberScrollState()

            // Set all of the values if we're editing. Only do this the first time though
            // as we need to allow that changes are made to this for editing
            if (taskToEdit.value != null && !didSetValues) {
                val task = taskToEdit.value!!
                title = task.title
                description = task.description ?: ""
                dueDate = task.dueDate
                reminderIndex = Utils.getReminderIndex(task.dueDate, task.reminder)
                reminderStartDate = task.reminder
                didSetValues = true
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
            ) {
                if (taskIdToEdit != null) {
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

                ReminderPicker(dueDate, reminderIndex, reminderStartDate) { index, startDate ->
                    reminderIndex = index
                    reminderStartDate = startDate
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
                    icon = Icons.Outlined.School,
                    isError = subjectIsError
                ) { navController?.navigate("subject_select_dialog") }

                if (taskIdToEdit != null) {
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp),
                        label = { Text(stringResource(R.string.add_task_description_label)) },
                    )
                }

                if (taskIdToEdit == null) {
                    Box(modifier = Modifier.padding(top = 20.dp)) // Spacer
                    repeat(viewModel.tasks.size) { index ->
                        TaskListItem(
                            viewModel.tasks[index],
                            dueDate,
                            viewModel,
                            index != 0, // as long as it's not the first one
                            index != viewModel.tasks.size - 1 // as long as it's not the last one
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskListItem(
    taskData: TaskData,
    dueDate: LocalDate?,
    viewModel: AddTaskViewModel,
    hasItemAbove: Boolean = false,
    hasItemBelow: Boolean = false
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var isExpanded by remember { mutableStateOf(false) }
    val arrowAngle by animateFloatAsState(
        targetValue = if (isExpanded) 360f else 180f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        )
    )

    val topCornerRadius = (if (hasItemAbove) 0 else 4).dp
    val bottomCornerRadius = (if (hasItemBelow) 0 else 4).dp

    Column(
        modifier = Modifier
            .padding(bottom = 2.dp)
            .clip(
                RoundedCornerShape(
                    topStart = topCornerRadius,
                    topEnd = topCornerRadius,
                    bottomStart = bottomCornerRadius,
                    bottomEnd = bottomCornerRadius
                )
            )
            .background(
                if (isSystemInDarkTheme()) AppColors.ContainerDark
                else AppColors.ContainerLight
            )
            .padding(bottom = 10.dp)
    ) {
        IconButton(
            onClick = { viewModel.tasks.remove(taskData) },
            enabled = viewModel.tasks.size > 1, // => The user can't have zero TaskListItems
            modifier = Modifier.align(Alignment.End)
        ) { Icon(Icons.Filled.Remove, "") }
        TextField(
            value = title,
            onValueChange = {
                title = it
                taskData.title = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp, end = 10.dp, start = 10.dp),
            maxLines = 1,
            label = { Text(stringResource(R.string.add_task_title_label)) },
        )

        TextField(
            value = description,
            onValueChange = {
                description = it
                taskData.description = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp, start = 10.dp),
            label = { Text(stringResource(R.string.add_task_description_label)) },
        )

        HorizontalSpacer(padding = PaddingValues(top = 20.dp, bottom = 13.dp))

        Row(modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .fillMaxWidth()
            .clickable {
                isExpanded = !isExpanded
            }) {
            Text(stringResource(R.string.add_task_additional_properties))
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "",
                    tint = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .rotate(arrowAngle)
                )
            }
        }
        Box(modifier = Modifier.animateContentSize()) {
            if (isExpanded) {
                Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp)) {
                    ReminderPicker(dueDate) { index, _ ->
                        taskData.reminderIndex = index
                    }
                }
            }
        }
    }
}

class AddTaskViewModel(application: Application) : AndroidViewModel(application) {
    private val _taskToEdit: MutableStateFlow<Task?> = MutableStateFlow(null)
    val taskToEdit: StateFlow<Task?> = _taskToEdit

    val tasks = mutableStateListOf(TaskData())

    fun setTaskId(taskId: Int) {
        viewModelScope.launch {
            AppDatabase.instance((getApplication() as Application).baseContext).taskDao()
                .findById(taskId).collect { _taskToEdit.value = it }
        }
    }

    fun insertAll(task: List<Task>) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.instance((getApplication() as Application).baseContext).taskDao()
                .insertAll(*task.toTypedArray())
        }
    }

    fun update(task: Task) {
        viewModelScope.launch {
            AppDatabase.instance((getApplication() as Application).baseContext).taskDao()
                .update(task)
        }
    }
}

data class TaskData(
    var title: String = "",
    var description: String = "",
    var reminderIndex: Int = -2
)

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
        .setTitle(context.resources.getString(R.string.pick_reminder_dialog_title))
        .setItems(R.array.reminder_choices) { _, which -> onSelected(which) }
        .show()
}

@Composable
private fun ReminderPicker(
    dueDate: LocalDate?,
    _reminderIndex: Int = -2,
    _reminderStartDate: LocalDate? = null,
    onReminderPicked: (index: Int, startDate: LocalDate) -> Unit
) {
    var reminderIndex by remember { mutableStateOf(_reminderIndex) }
    var reminderStartDate: LocalDate? by remember { mutableStateOf(_reminderStartDate) }

    // These values only update on the 3rd recomposition or so
    if (_reminderIndex != -2) reminderIndex = _reminderIndex
    if (_reminderStartDate != null) reminderStartDate = _reminderStartDate

    val context = LocalContext.current

    HorizontalButton(
        text = if (reminderStartDate == null || reminderIndex == -2) {
            stringResource(R.string.add_task_reminder_selector)
        } else stringArrayResource(R.array.reminder_choices)[reminderIndex],
        icon = Icons.Outlined.Notifications,
    ) {
        pickReminder(context) {
            reminderIndex = it

            fun dueDate() = dueDate ?: LocalDate.now()

            reminderStartDate = if (reminderIndex == 0)
                dueDate()
            else {
                val daysDifference = when (reminderIndex) {
                    in 0..4 -> reminderIndex
                    5 -> 7
                    6 -> 14
                    else -> -1
                }.toLong()
                dueDate().minusDays(daysDifference)
            }

            onReminderPicked(reminderIndex, reminderStartDate!!)
        }
    }
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