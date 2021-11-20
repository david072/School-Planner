package de.david072.schoolplanner.screens

import android.app.Application
import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowColumn
import de.david072.schoolplanner.database.AppDatabase
import de.david072.schoolplanner.database.entities.Subject
import de.david072.schoolplanner.database.entities.Task
import de.david072.schoolplanner.ui.AppTopAppBar
import de.david072.schoolplanner.ui.theme.SchoolPlannerTheme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HomeScreen(navController: NavController?) {
    Scaffold(topBar = {
        AppTopAppBar(navController, actions = {
            IconButton(onClick = { navController?.navigate("settings") }) {
                Icon(Icons.Outlined.Settings, "")
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { navController?.navigate("add_task") },
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Icon(Icons.Filled.Add, "")
        }
    }) {
        val viewModel = viewModel<HomeScreenViewModel>()
        Box(modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
            val dates = viewModel.dates.collectAsState()
            LazyColumn {
                items(dates.value.keys.size) { index ->
                    // The key represents the date (as an epoch day)
                    val key = dates.value.keys.elementAt(index)
                    DateListItem(navController, viewModel, key, dates.value[key])
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
    subjectGroups: ArrayList<SubjectGroup>?
) {
    var isExpanded by remember { mutableStateOf(true) }

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
            Row {
                Text(
                    LocalDate.ofEpochDay(date)
                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
                    style = MaterialTheme.typography.h6
                )

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
                if (isSystemInDarkTheme()) Color(35, 35, 35)
                else Color(240, 240, 240)
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
                subjectGroup.subject.name,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            )
            FlowColumn(modifier = Modifier.padding(top = 5.dp), mainAxisSpacing = 2.dp) {
                repeat(subjectGroup.tasks.size) { index ->
                    TaskListItem(subjectGroup.tasks[index], navController, viewModel)
                }
            }
        }
    }
}

@Composable
fun TaskListItem(task: Task, navController: NavController?, viewModel: HomeScreenViewModel) {
    var completed by remember { mutableStateOf(task.completed) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        Checkbox(checked = completed, onCheckedChange = {
            viewModel.setCompleted(task, it)
            completed = it
        })

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .clickable {
                    navController?.navigate("view_task/${task.uid}")
                }, contentAlignment = Alignment.CenterStart
        ) {
            Text(task.title)
        }
    }
}

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val _dates: MutableStateFlow<MutableMap<Long, ArrayList<SubjectGroup>>> =
        MutableStateFlow(mutableMapOf())
    val dates: StateFlow<MutableMap<Long, ArrayList<SubjectGroup>>> = _dates

    init {
        viewModelScope.launch {
            val appDatabase =
                AppDatabase.instance((getApplication() as Application).applicationContext)
            appDatabase.taskDao().getOrderedByDueDate().collect {
                // Temporary variable that is emitted into the flow _dates later.
                // This is necessary, since otherwise the state won't update above.
                val generatedMap: MutableMap<Long, ArrayList<SubjectGroup>> = mutableMapOf()
                it.forEach { task ->
                    val epochDay = task.dueDate.toEpochDay()
                    if (generatedMap[epochDay] == null) {
                        val subject = appDatabase.subjectDao().findById(task.subjectId).first()
                        generatedMap[epochDay] =
                            arrayListOf(SubjectGroup(subject, arrayListOf(task)))
                        return@forEach
                    }

                    var didAddTask = false
                    run tryInsertTask@{
                        generatedMap[epochDay]!!.forEach subjectGroupLoop@{ subjectGroup ->
                            if (subjectGroup.subject.uid != task.subjectId) return@subjectGroupLoop
                            subjectGroup.tasks.add(task)
                            didAddTask = true
                            // Break out of loop, since we inserted the task
                            return@tryInsertTask
                        }
                    }

                    if (!didAddTask) {
                        val subject = appDatabase.subjectDao().findById(task.subjectId).first()
                        generatedMap[epochDay]!!.add(SubjectGroup(subject, arrayListOf(task)))
                    }
                }
                _dates.emit(generatedMap)
            }
        }
    }

    fun setCompleted(task: Task, completed: Boolean) {
        viewModelScope.launch {
            task.completed = completed
            AppDatabase.instance((getApplication() as Application).applicationContext).taskDao()
                .update(task)
        }
    }
}

data class SubjectGroup(val subject: Subject, val tasks: ArrayList<Task>)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(locale = "de")
@Composable
private fun Preview() {
    SchoolPlannerTheme {
        HomeScreen(null)
    }
}