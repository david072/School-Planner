package de.david072.schoolplanner.screens

import android.app.Application
import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Task
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.david072.schoolplanner.R
import de.david072.schoolplanner.database.entities.Exam
import de.david072.schoolplanner.database.entities.Subject
import de.david072.schoolplanner.database.entities.Task
import de.david072.schoolplanner.database.repositories.ExamRepository
import de.david072.schoolplanner.database.repositories.TaskRepository
import de.david072.schoolplanner.ui.AppTopAppBar
import de.david072.schoolplanner.ui.HorizontalSpacer
import de.david072.schoolplanner.ui.theme.AppColors
import de.david072.schoolplanner.ui.theme.SchoolPlannerTheme
import de.david072.schoolplanner.util.Utils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(navController: NavController?) {
    Scaffold(topBar = {
        AppTopAppBar(navController, actions = {
            IconButton(onClick = { navController?.navigate("settings") }) {
                Icon(Icons.Outlined.Settings, "")
            }
        })
    }, floatingActionButton = {
        var isExpanded by remember { mutableStateOf(false) }

        val angle by animateFloatAsState(
            targetValue = if (isExpanded) 135f else 0f,
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            )
        )

        val color by animateColorAsState(
            targetValue = if (isExpanded) MaterialTheme.colors.error else MaterialTheme.colors.primary
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val animation = @Composable { content: @Composable AnimatedVisibilityScope.() -> Unit ->
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                    content = content
                )
            }

            animation {
                FloatingActionButton(
                    onClick = { navController?.navigate("add_test") },
                    backgroundColor = MaterialTheme.colors.primary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Outlined.Class, "")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            animation {
                FloatingActionButton(
                    onClick = { navController?.navigate("add_task") },
                    backgroundColor = MaterialTheme.colors.primary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Outlined.Task, "")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            FloatingActionButton(
                onClick = { isExpanded = !isExpanded },
                backgroundColor = color,
                modifier = Modifier.rotate(angle)
            ) {
                Icon(Icons.Filled.Add, "", tint = Color.Black)
            }
        }
    }) {
        val viewModel = viewModel<HomeScreenViewModel>()
        Box(modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
            val groups = remember { viewModel.groups }
            LazyColumn {
                items(viewModel.dates.size) { index ->
                    // The key represents the date (as an epoch day)
                    val date = viewModel.dates[index]
                    DateListItem(navController, viewModel, date, groups[date])
                }
            }
        }
    }
}

@Composable
fun DateListItem(
    navController: NavController?,
    viewModel: HomeScreenViewModel,
    date: Long,
    subjectGroups: SnapshotStateList<SubjectGroup>?
) {
    var isExpanded by rememberSaveable {
        mutableStateOf(
            LocalDate.ofEpochDay(date).isBefore(LocalDate.now().plusMonths(1))
        )
    }

    val arrowAngle by animateFloatAsState(
        targetValue = if (isExpanded) 360f else 180f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        )
    )

    Column(modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                var exams = 0
                var tasksLeft = 0
                subjectGroups?.forEach { subjectGroup ->
                    exams += subjectGroup.exams.size
                    tasksLeft += subjectGroup.tasks.count { !it.completed.value }
                }

                Column {
                    Text(
                        Utils.formattedDate(date, LocalContext.current),
                        style = MaterialTheme.typography.h6
                    )

                    Text(
                        stringResource(R.string.home_date_group_caption)
                            .replace("%exams%", exams.toString())
                            .replace("%tasksLeft%", tasksLeft.toString()),
                        style = MaterialTheme.typography.caption.copy(color = Color.Gray)
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
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
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .animateContentSize()
        ) {
            subjectGroups?.let {
                repeat(it.size) { index ->
                    SubjectListItem(
                        navController,
                        viewModel,
                        it[index],
                        index != 0, // as long as it's not the first one
                        index != it.size - 1, // as long as it's not the last one
                        isExpanded
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectListItem(
    navController: NavController?,
    viewModel: HomeScreenViewModel,
    subjectGroup: SubjectGroup,
    hasItemAbove: Boolean,
    hasItemBelow: Boolean,
    isExpanded: Boolean
) {
    val topCornerRadius = (if (hasItemAbove) 0 else 10).dp
    val bottomCornerRadius = (if (hasItemBelow) 0 else 10).dp
    Column(
        modifier = Modifier
            // Margin
            .padding(
                bottom = (if (hasItemBelow) 1 else 0).dp,
                top = (if (hasItemAbove) 1 else 0).dp
            )
            .fillMaxWidth()
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
            .run {
                // Padding
                return@run if (isExpanded) padding(
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 10.dp,
                    top = 7.dp
                ) else this
            }
    ) {
        if (isExpanded) {
            Text(
                subjectGroup.subject.value.name,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            )
            Column(modifier = Modifier.padding(top = 5.dp)) {
                repeat(subjectGroup.exams.size) { index ->
                    Text(
                        subjectGroup.exams[index].title,
                        modifier = Modifier.clickable {
                            navController?.navigate("view_test/${subjectGroup.exams[index].uid}")
                        })
                }

                if (subjectGroup.exams.isNotEmpty() && subjectGroup.tasks.isNotEmpty())
                    HorizontalSpacer(padding = PaddingValues(top = 12.dp, bottom = 8.dp))

                repeat(subjectGroup.tasks.size) { index ->
                    TaskListItem(subjectGroup.tasks[index], navController, viewModel)
                }
            }
        }
    }
}

// TODO: Add some sort of animation when the task moves in the list
@Composable
fun TaskListItem(task: StateTask, navController: NavController?, viewModel: HomeScreenViewModel) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        Checkbox(checked = task.completed.value, onCheckedChange = {
            viewModel.setCompleted(task.task, it)
        })

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .clickable {
                    navController?.navigate("view_task/${task.uid.value}")
                }, contentAlignment = Alignment.CenterStart
        ) {
            Text(task.title.value)
        }
    }
}

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {
    val groups = mutableStateMapOf<Long, SnapshotStateList<SubjectGroup>>()
    val dates = mutableStateListOf<Long>()

    private var didStartExams = false

    init {
        // FIXME: These two function are basically the same... Join them somehow?
        getTasks(application)
        //getExams(application)
    }

    private fun getTasks(application: Application) {
        viewModelScope.launch {
            val taskRepository = TaskRepository(application)
            val groups = this@HomeScreenViewModel.groups
            taskRepository.getOrderedByDueDate().collect {
                // Remove tasks that are in the new list and in our groups,
                // so that we can "re-add" them later
                groups.forEach { (key, subjectGroups) ->
                    subjectGroups.forEach { subjectGroup ->
                        subjectGroup.tasks.forEach { task ->
                            if (!it.contains(task.task)) subjectGroup.tasks.remove(task)
                        }

                        if (subjectGroup.tasks.isEmpty() && subjectGroup.exams.isEmpty())
                            subjectGroups.remove(subjectGroup)
                    }

                    if (subjectGroups.isEmpty()) {
                        groups.remove(key)
                        dates.remove(key)
                    }
                }

                it.forEach processTasks@{ task ->
                    val epochDay = task.dueDate.toEpochDay()
                    // Delete the task if the due date passed
                    if (task.dueDate.toEpochDay() < LocalDate.now().toEpochDay()) {
                        launch { taskRepository.delete(task) }
                        return@processTasks
                    }

                    if (groups[epochDay] != null) {
                        groups[epochDay]!!.forEach findSubjectGroup@{ subjectGroup ->
                            if (subjectGroup.subject.value.uid != task.subjectId) return@findSubjectGroup

                            for (i in 0 until subjectGroup.tasks.size) {
                                val subjectGroupTask = subjectGroup.tasks[i]
                                if (subjectGroupTask.uid.value == task.uid) {
                                    if (subjectGroupTask != StateTask(task)) subjectGroup.tasks[i] =
                                        StateTask(task)
                                    return@processTasks
                                }
                            }

                            subjectGroup.tasks.add(StateTask(task))
                            return@processTasks
                        }

                        groups[epochDay]!!.add(
                            SubjectGroup(
                                mutableStateOf(task.getSubject(application)),
                                mutableStateListOf(StateTask(task))
                            )
                        )
                    } else {
                        groups[epochDay] = mutableStateListOf(
                            SubjectGroup(
                                mutableStateOf(task.getSubject(application)),
                                mutableStateListOf(StateTask(task))
                            )
                        )

                        addDate(epochDay)
                    }
                }

                // FIXME: Dumb hack, but these two functions can't run in parallel for some reason.
                //  Since collect never finishes we have to check that we don't start it twice.
                if (!didStartExams) {
                    getExams(application)
                    didStartExams = true
                }
            }
        }
    }

    private fun getExams(application: Application) {
        viewModelScope.launch {
            val examRepository = ExamRepository(application)
            val groups = this@HomeScreenViewModel.groups
            examRepository.getOrderedByDueDate().collect {
                groups.forEach { (key, subjectGroups) ->
                    subjectGroups.forEach { subjectGroup ->
                        subjectGroup.exams.forEach { exam ->
                            if (!it.contains(exam)) subjectGroup.exams.remove(exam)
                        }

                        if (subjectGroup.tasks.isEmpty() && subjectGroup.exams.isEmpty())
                            subjectGroups.remove(subjectGroup)
                    }

                    if (subjectGroups.isEmpty()) {
                        groups.remove(key)
                        dates.remove(key)
                    }
                }

                it.forEach processTasks@{ exam ->
                    val epochDay = exam.dueDate.toEpochDay()
                    // Delete the exam if the due date passed
                    if (exam.dueDate.toEpochDay() < LocalDate.now().toEpochDay()) {
                        launch { examRepository.delete(exam) }
                        return@processTasks
                    }

                    if (groups[epochDay] != null) {
                        groups[epochDay]!!.forEach findSubjectGroup@{ subjectGroup ->
                            if (subjectGroup.subject.value.uid != exam.subjectId) return@findSubjectGroup

                            for (i in 0 until subjectGroup.exams.size) {
                                val subjectGroupExam = subjectGroup.exams[i]
                                if (subjectGroupExam.uid == exam.uid) {
                                    if (subjectGroupExam != exam) subjectGroup.exams[i] = exam
                                    return@processTasks
                                }
                            }

                            subjectGroup.exams.add(exam)
                            return@processTasks
                        }

                        groups[epochDay]!!.add(
                            SubjectGroup(
                                mutableStateOf(exam.getSubject(application)),
                                exams = mutableStateListOf(exam)
                            )
                        )
                    } else {
                        groups[epochDay] = mutableStateListOf(
                            SubjectGroup(
                                mutableStateOf(exam.getSubject(application)),
                                exams = mutableStateListOf(exam)
                            )
                        )

                        addDate(epochDay)
                    }
                }
            }
        }
    }

    private fun addDate(newDate: Long) {
        if (dates.contains(newDate)) return

        if (dates.isEmpty()) dates.add(newDate)
        else {
            // Insert in such a way, so that dates are sorted from most recent to furthest in the future
            var didInsert = false
            for (i in 0 until dates.size) {
                if (dates[i] > newDate) {
                    dates.add(if (i == 0) 0 else i - 1, newDate)
                    didInsert = true
                    break
                }
            }
            if (!didInsert) dates.add(newDate)
        }
    }

    fun setCompleted(task: Task, completed: Boolean) {
        viewModelScope.launch {
            task.completed = completed
            TaskRepository(getApplication()).update(task)
        }
    }
}

data class SubjectGroup(
    val subject: MutableState<Subject>,
    val tasks: SnapshotStateList<StateTask> = mutableStateListOf(),
    val exams: SnapshotStateList<Exam> = mutableStateListOf()
)

data class StateTask(
    val uid: MutableState<Int>,
    val title: MutableState<String>,
    var completed: MutableState<Boolean>,
    val task: Task
) {
    constructor(task: Task) : this(
        mutableStateOf(task.uid),
        mutableStateOf(task.title),
        mutableStateOf(task.completed),
        task
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(locale = "de")
@Composable
private fun Preview() {
    SchoolPlannerTheme {
        HomeScreen(null)
    }
}