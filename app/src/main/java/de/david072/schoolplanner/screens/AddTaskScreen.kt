package de.david072.schoolplanner.screens

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Parcel
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.david072.schoolplanner.R
import de.david072.schoolplanner.database.entities.Exam
import de.david072.schoolplanner.database.entities.Task
import de.david072.schoolplanner.database.repositories.ExamRepository
import de.david072.schoolplanner.database.repositories.SubjectRepository
import de.david072.schoolplanner.database.repositories.TaskRepository
import de.david072.schoolplanner.ui.AppTopAppBar
import de.david072.schoolplanner.ui.HorizontalButton
import de.david072.schoolplanner.ui.HorizontalSpacer
import de.david072.schoolplanner.ui.theme.AppColors
import de.david072.schoolplanner.ui.theme.SchoolPlannerTheme
import de.david072.schoolplanner.util.Utils
import de.david072.schoolplanner.util.swipeToDelete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@Composable
fun AddTaskScreen(
    navController: NavController?,
    isExam: Boolean = false,
    taskIdToEdit: Int? = null
) {
    val context = LocalContext.current
    val viewModel = viewModel<AddTaskViewModel>()

    if (taskIdToEdit != null) viewModel.setTaskId(taskIdToEdit, isExam)
    val taskToEdit = viewModel.taskToEdit.collectAsState()
    val examToEdit = viewModel.examToEdit.collectAsState()

    var didSetValues by remember { mutableStateOf(false) }

    var subjectName: String? by remember { mutableStateOf(null) }
    val subjectIdLiveData = navController?.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Int>("subject_id")

    val subjectIdObserver = Observer<Int> {
        viewModel.parentTaskData.value.subjectId.apply setSubjectId@{
            value = value.copy(value = it, isError = false)
        }
        subjectName = null
    }

    LaunchedEffect(Unit) {
        subjectIdLiveData?.observeForever(subjectIdObserver)
    }

    DisposableEffect(Unit) {
        onDispose { subjectIdLiveData?.removeObserver(subjectIdObserver) }
    }

    if (subjectName == null && viewModel.parentTaskData.value.subjectId.value.value != null) {
        val subjectQueryState =
            SubjectRepository(context).findById(viewModel.parentTaskData.value.subjectId.value.value!!)
                .collectAsState(initial = null)
        if (subjectQueryState.value != null) subjectName =
            subjectQueryState.value!!.name
    }

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    // TODO: Remove this function and add a more visible error indicator on the text fields,
    //  at best with a message
    fun showErrorSnackbar() {
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = context.getString(R.string.add_task_fields_missing_error)
            )
        }
    }

    Scaffold(scaffoldState = scaffoldState, topBar = {
        AppTopAppBar(navController, true, actions = {
            if (taskIdToEdit == null) {
                IconButton(onClick = { viewModel.taskDatas.add(TaskData()) }) {
                    Icon(Icons.Filled.Add, "")
                }
            }
        })
    }, floatingActionButton = {
        ExtendedFloatingActionButton(onClick = {
            var failed = false
            if (!viewModel.parentTaskData.value.validate(true))
                failed = true

            if (taskToEdit.value != null || examToEdit.value != null) {
                if (!failed) {
                    if (!isExam) {
                        Task(
                            uid = taskToEdit.value!!.uid,
                            title = viewModel.parentTaskData.value.title.value.value,
                            dueDate = viewModel.parentTaskData.value.dueDate.value.value!!,
                            reminder = viewModel.parentTaskData.value.getReminderStartDate()!!,
                            subjectId = viewModel.parentTaskData.value.subjectId.value.value!!,
                            description = viewModel.parentTaskData.value.description.value.value,
                            completed = taskToEdit.value!!.completed
                        ).let { viewModel.updateTask(it) }
                    } else {
                        Exam(
                            uid = examToEdit.value!!.uid,
                            title = viewModel.parentTaskData.value.title.value.value,
                            dueDate = viewModel.parentTaskData.value.dueDate.value.value!!,
                            reminder = viewModel.parentTaskData.value.getReminderStartDate()!!,
                            subjectId = viewModel.parentTaskData.value.subjectId.value.value!!,
                            description = viewModel.parentTaskData.value.description.value.value
                        ).let { viewModel.updateExam(it) }
                    }
                }
            } else {
                val result = arrayListOf<Any>()
                viewModel.taskDatas.forEach {
                    if (!it.validate() || failed) {
                        if (!failed) failed = true
                        return@forEach
                    }

                    val reminder =
                        if (it.reminderIndex.value.value == -2 ||
                            it.reminderIndex.value.value == viewModel.parentTaskData.value.reminderIndex.value.value
                        ) viewModel.parentTaskData.value.getReminderStartDate()!!
                        else it.getReminderStartDate(viewModel.parentTaskData.value.dueDate.value.value)!!

                    result += if (!isExam) Task(
                        title = it.title.value.value,
                        dueDate = viewModel.parentTaskData.value.dueDate.value.value!!,
                        reminder = reminder,
                        subjectId = it.subjectId.value.value
                            ?: viewModel.parentTaskData.value.subjectId.value.value!!,
                        description = it.description.value.value,
                        completed = false
                    )
                    else Exam(
                        title = it.title.value.value,
                        dueDate = viewModel.parentTaskData.value.dueDate.value.value!!,
                        reminder = reminder,
                        subjectId = it.subjectId.value.value
                            ?: viewModel.parentTaskData.value.subjectId.value.value!!,
                        description = it.description.value.value,
                    )
                }

                if (!failed) {
                    if (result.isEmpty()) return@ExtendedFloatingActionButton

                    if (!isExam) viewModel.insertAllTasks(result as ArrayList<Task>)
                    else viewModel.insertAllExams(result as ArrayList<Exam>)
                }
            }

            if (failed) {
                showErrorSnackbar()
                return@ExtendedFloatingActionButton
            }

            navController?.popBackStack()
        }, icon = {
            Icon(
                if (taskToEdit.value == null && examToEdit.value == null) Icons.Filled.Add
                else Icons.Outlined.Save,
                ""
            )
        }, text = {
            Text(
                if (taskToEdit.value == null && examToEdit.value == null)
                    stringResource(if (!isExam) R.string.add_task_button else R.string.add_test_button)
                else stringResource(R.string.general_save)
            )
        }, backgroundColor = MaterialTheme.colors.primary)
    }) {
        Box(modifier = Modifier.padding(PaddingValues(all = 10.dp))) {
            val scrollState = rememberScrollState()

            // Set all of the values if we're editing. Only do this the first time though
            // as we need to allow that changes are made to this for editing
            if ((taskToEdit.value != null || examToEdit.value != null) && !didSetValues) {
                val taskDueDate =
                    if (taskToEdit.value != null) taskToEdit.value!!.dueDate
                    else examToEdit.value!!.dueDate

                viewModel.parentTaskData.value.apply {
                    title.value = title.value.copy(
                        value = if (taskToEdit.value != null) taskToEdit.value!!.title
                        else examToEdit.value!!.title
                    )
                    description.value = description.value.copy(
                        value = (if (taskToEdit.value != null) taskToEdit.value!!.description
                        else examToEdit.value!!.description) ?: ""
                    )
                    dueDate.value = dueDate.value.copy(value = taskDueDate)
                    reminderIndex.value = reminderIndex.value.copy(
                        value = Utils.getReminderIndex(
                            taskDueDate,
                            if (taskToEdit.value != null) taskToEdit.value!!.reminder
                            else examToEdit.value!!.reminder
                        )
                    )
                    subjectId.value = subjectId.value.copy(
                        value = if (taskToEdit.value != null) taskToEdit.value!!.subjectId
                        else examToEdit.value!!.subjectId
                    )
                }
                didSetValues = true
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
            ) {
                if (taskIdToEdit != null) {
                    TextField(
                        value = viewModel.parentTaskData.value.title.value.value,
                        onValueChange = {
                            viewModel.parentTaskData.value.title.apply {
                                value = value.copy(value = it)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 15.dp),
                        maxLines = 1,
                        label = { Text(stringResource(R.string.add_task_title_label)) },
                    )
                }

                HorizontalButton(
                    text = if (viewModel.parentTaskData.value.dueDate.value.value == null)
                        stringResource(R.string.add_task_due_date_selector)
                    else viewModel.parentTaskData.value.dueDate.value.value!!.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                    ),
                    icon = Icons.Outlined.Event,
                    end = { if (viewModel.parentTaskData.value.dueDate.value.isError) ErrorIcon() }
                ) {
                    pickDate(context, viewModel.parentTaskData.value.dueDate.value.value) {
                        viewModel.parentTaskData.value.dueDate.apply {
                            value = value.copy(value = it, isError = false)
                        }
                    }
                }
                HorizontalSpacer()

                ReminderPicker(
                    viewModel.parentTaskData.value.reminderIndex.value.value,
                    viewModel.parentTaskData.value.getReminderStartDate(),
                    end = { if (viewModel.parentTaskData.value.reminderIndex.value.isError) ErrorIcon() }
                ) { index ->
                    viewModel.parentTaskData.value.apply {
                        reminderIndex.value =
                            reminderIndex.value.copy(value = index, isError = false)
                    }
                }
                HorizontalSpacer()

                HorizontalButton(
                    text = subjectName ?: stringResource(R.string.add_task_subject_selector),
                    icon = Icons.Outlined.School,
                    end = { if (viewModel.parentTaskData.value.subjectId.value.isError) ErrorIcon() }
                ) { navController?.navigate("subject_select_dialog") }

                if (taskIdToEdit != null) {
                    TextField(
                        value = viewModel.parentTaskData.value.description.value.value,
                        onValueChange = {
                            viewModel.parentTaskData.value.description.apply {
                                value = value.copy(value = it)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp),
                        label = { Text(stringResource(R.string.add_task_description_label)) },
                    )
                }

                if (taskIdToEdit == null) {
                    Box(modifier = Modifier.padding(top = 20.dp)) // Spacer
                    repeat(viewModel.taskDatas.size) { index ->
                        TaskListItem(
                            navController,
                            index,
                            viewModel.taskDatas.size,
                            viewModel.taskDatas[index],
                            viewModel.parentTaskData.value.dueDate.value.value ?: LocalDate.now()
                        ) { viewModel.taskDatas.removeAt(index) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskListItem(
    navController: NavController?,
    index: Int,
    taskDatasSize: Int,
    taskData: TaskData,
    dueDate: LocalDate,
    onDeleted: () -> Unit,
) {
    var subjectName: String? by remember { mutableStateOf(null) }
    val subjectIdLiveData = navController?.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Int>("subject_id_$index")

    val subjectIdObserver = Observer<Int> {
        taskData.subjectId.apply {
            value = value.copy(value = it, isError = false)
        }
    }

    LaunchedEffect(Unit) {
        subjectIdLiveData?.observeForever(subjectIdObserver)
    }

    DisposableEffect(Unit) {
        onDispose { subjectIdLiveData?.removeObserver(subjectIdObserver) }
    }

    if (taskData.subjectId.value.value != null) {
        val subjectQueryState =
            SubjectRepository(LocalContext.current).findById(taskData.subjectId.value.value!!)
                .collectAsState(initial = null)
        if (subjectQueryState.value != null) subjectName =
            subjectQueryState.value!!.name
    }

    var title by remember { taskData.title }
    var description by remember { taskData.description }

    var isExpanded by remember { mutableStateOf(false) }
    val arrowAngle by animateFloatAsState(
        targetValue = if (isExpanded) 360f else 180f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        )
    )

    val topCornerRadius = (if (index != 0) 0 else 4).dp
    val bottomCornerRadius = (if (index != taskDatasSize - 1) 0 else 4).dp

    val offsetX = remember { Animatable(0f) }
    Column(
        modifier = Modifier
            .padding(bottom = 2.dp)
            .swipeToDelete(
                offsetX,
                (LocalConfiguration.current.screenWidthDp * 3).toFloat(),
                enabled = taskDatasSize > 1
            ) { onDeleted() }
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
            .animateContentSize()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (taskData.title.value.isError || taskData.reminderIndex.value.isError || taskData.subjectId.value.isError) {
                ErrorIcon(modifier = Modifier.padding(start = 10.dp))
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                IconButton(
                    onClick = { onDeleted() },
                    enabled = taskDatasSize > 1, // => The user can't have zero TaskListItems
                ) { Icon(Icons.Filled.Remove, "") }
            }
        }
        TextField(
            value = title.value,
            onValueChange = { title = TaskAttribute(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp, end = 10.dp, start = 10.dp),
            maxLines = 1,
            label = { Text(stringResource(R.string.add_task_title_label)) },
        )

        TextField(
            value = description.value,
            onValueChange = { description = TaskAttribute(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp, start = 10.dp),
            label = { Text(stringResource(R.string.add_task_description_label)) },
        )

        if (taskDatasSize > 1) {
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
                        ReminderPicker(
                            taskData.reminderIndex.value.value,
                            taskData.getReminderStartDate(dueDate),
                            end = {
                                if (taskData.reminderIndex.value.value == -2) return@ReminderPicker

                                IconButton(onClick = {
                                    taskData.reminderIndex.apply { value = value.copy(value = -2) }
                                }) {
                                    Icon(Icons.Outlined.Close, "", tint = Color.Red)
                                }
                            }) { index ->
                            taskData.reminderIndex.value =
                                taskData.reminderIndex.value.copy(value = index)
                        }

                        HorizontalButton(
                            text = subjectName
                                ?: stringResource(R.string.add_task_subject_selector),
                            icon = Icons.Outlined.School,
                            end = {
                                if (taskData.subjectId.value.value != null) {
                                    IconButton(onClick = {
                                        taskData.subjectId.apply {
                                            value = value.copy(value = null)
                                        }
                                        subjectName = null
                                    }) {
                                        Icon(Icons.Outlined.Close, "", tint = Color.Red)
                                    }
                                }
                            }
                        ) { navController?.navigate("subject_select_dialog?id=$index") }
                    }
                }
            }
        }
        // Collapse the properties field, since it shouldn't be opened when it becomes
        // visible again
        else {
            taskData.apply {
                reminderIndex.value = reminderIndex.value.copy(value = -2)
                subjectId.value = subjectId.value.copy(value = null)
            }
            isExpanded = false
        }
    }
}

class AddTaskViewModel(application: Application) : AndroidViewModel(application) {
    val parentTaskData = mutableStateOf(TaskData())
    val taskDatas = mutableStateListOf(TaskData())

    private val _taskToEdit: MutableStateFlow<Task?> = MutableStateFlow(null)
    private val _examToEdit: MutableStateFlow<Exam?> = MutableStateFlow(null)
    val taskToEdit: StateFlow<Task?> = _taskToEdit
    val examToEdit: StateFlow<Exam?> = _examToEdit

    fun setTaskId(taskId: Int, isExam: Boolean = false) {
        viewModelScope.launch {
            val flow =
                if (!isExam) TaskRepository(getApplication()).findById(taskId) else ExamRepository(
                    getApplication()
                ).findById(taskId)
            flow.collect {
                if (it is Task) _taskToEdit.value = it
                else if (it is Exam) _examToEdit.value = it
            }
        }
    }

    fun insertAllTasks(tasks: List<Task>) {
        viewModelScope.launch(Dispatchers.IO) {
            TaskRepository(getApplication()).insertAll(*tasks.toTypedArray())
        }
    }

    fun insertAllExams(exams: List<Exam>) {
        viewModelScope.launch {
            ExamRepository(getApplication()).insertAll(*exams.toTypedArray())
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            TaskRepository(getApplication()).update(task)
        }
    }

    fun updateExam(exam: Exam) {
        viewModelScope.launch {
            ExamRepository(getApplication()).update(exam)
        }
    }
}

data class TaskData(
    var title: MutableState<TaskAttribute<String>> = mutableStateOf(TaskAttribute("")),
    var description: MutableState<TaskAttribute<String>> = mutableStateOf(TaskAttribute("")),
    var dueDate: MutableState<TaskAttribute<LocalDate?>> = mutableStateOf(TaskAttribute(null)),
    var reminderIndex: MutableState<TaskAttribute<Int>> = mutableStateOf(TaskAttribute(-2)),
    var subjectId: MutableState<TaskAttribute<Int?>> = mutableStateOf(TaskAttribute(null))
) {

    fun validate(isParentData: Boolean = false): Boolean {
        var isValid = true
        val trimmedTitle = title.value.value.trim()

        if (!isParentData) {
            if (trimmedTitle.isEmpty()) {
                title.value = title.value.copy(trimmedTitle, true)
                isValid = false
            }
        }

        if (isParentData) {
            if (reminderIndex.value.value == -2) {
                reminderIndex.value = reminderIndex.value.copy(isError = true)
                isValid = false
            }
            if (dueDate.value.value == null) {
                dueDate.value = dueDate.value.copy(isError = true)
                isValid = false
            }
            if (subjectId.value.value == null) {
                subjectId.value = subjectId.value.copy(isError = true)
                isValid = false
            }
        }

        return isValid
    }

    fun getReminderStartDate(_dueDate: LocalDate? = null): LocalDate? {
        val index = reminderIndex.value.value
        val dueDate = _dueDate ?: dueDate.value.value

        if (index == -2) return null
        if (index == 0) return dueDate

        val daysDifference = when (index) {
            in 0..4 -> index
            5 -> 7
            6 -> 14
            else -> -1
        }.toLong()

        return if (dueDate != null) dueDate.minusDays(daysDifference) else LocalDate.now()
            .minusDays(daysDifference)
    }
}

data class TaskAttribute<T>(
    val value: T,
    val isError: Boolean = false
)

private fun pickDate(
    context: Context,
    selection: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit
) {
    MaterialDatePicker.Builder
        .datePicker()
        .setCalendarConstraints(
            CalendarConstraints.Builder()
                .setOpenAt(
                    Calendar.getInstance().let {
                        when {
                            selection != null -> it.apply {
                                set(Calendar.MONTH, selection.monthValue - 1)
                                set(Calendar.YEAR, selection.year)
                            }
                            it.get(Calendar.DAY_OF_MONTH) == YearMonth.now()
                                .lengthOfMonth() -> it.apply { add(Calendar.MONTH, 1) }
                            else -> it
                        }
                    }.timeInMillis
                )
                .setStart(LocalDate.now().monthValue.toLong())
                .setValidator(object : CalendarConstraints.DateValidator {
                    override fun describeContents(): Int = 0
                    override fun writeToParcel(dest: Parcel?, flags: Int) {}

                    // Can only select tomorrow and days after that
                    override fun isValid(date: Long): Boolean =
                        date > Calendar.getInstance().timeInMillis
                }).build()
        )
        .let {
            if (selection != null) {
                it.setSelection(
                    Calendar.getInstance()
                        .apply {
                            set(Calendar.DAY_OF_MONTH, selection.dayOfMonth)
                            // LocalDate december: 12 Calendar december: 11 :/
                            set(Calendar.MONTH, selection.monthValue - 1)
                            set(Calendar.YEAR, selection.year)
                        }.timeInMillis
                )
            } else it
        }
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
    reminderIndex: Int,
    reminderStartDate: LocalDate?,
    end: @Composable (BoxScope.() -> Unit)? = null,
    onReminderPicked: (index: Int) -> Unit
) {
    val context = LocalContext.current

    HorizontalButton(
        text = if (reminderStartDate == null || reminderIndex == -2) {
            stringResource(R.string.add_task_reminder_selector)
        } else stringArrayResource(R.array.reminder_choices)[reminderIndex],
        icon = Icons.Outlined.Notifications,
        end = end
    ) {
        pickReminder(context) {
            onReminderPicked(it)
        }
    }
}

@Composable
private fun ErrorIcon(modifier: Modifier = Modifier) = Icon(
    Icons.Outlined.Error,
    "",
    tint = Color.Red,
    modifier = modifier
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(locale = "de")
@Composable
private fun Preview() {
    SchoolPlannerTheme {
        AddTaskScreen(null)
    }
}
