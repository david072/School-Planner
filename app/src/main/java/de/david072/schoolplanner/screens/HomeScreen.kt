package de.david072.schoolplanner.screens

import android.app.Application
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                    DateListItem(key, dates.value[key])
                }
            }
        }
    }
}

@Composable
fun DateListItem(date: Long, subjectGroups: ArrayList<SubjectGroup>?) {
    Column(modifier = Modifier.padding(bottom = 30.dp)) {
        Text(
            LocalDate.ofEpochDay(date).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
            style = MaterialTheme.typography.h5
        )
        FlowColumn {
            subjectGroups?.let {
                repeat(it.size) { index ->
                    Text("Subject: ${it[index].subject.name}")
                    FlowColumn {
                        repeat(it[index].tasks.size) { taskIndex ->
                            Text("Task: ${it[index].tasks[taskIndex].title}")
                        }
                    }
                }
            }
        }
    }
}

/*@Composable
fun SubjectListItem(
    subject: Subject,
    viewModel: HomeScreenViewModel,
    navController: NavController?
) {
    val tasks = viewModel.getTasks(subject.uid).collectAsState(initial = emptyList())
    var isExpanded by remember { mutableStateOf(true) }

    val arrowAngle by animateFloatAsState(
        targetValue = if (isExpanded) 360f else 180f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Row(modifier = Modifier
            .clickable { isExpanded = !isExpanded }
            .fillMaxWidth()) {
            Text(text = subject.name, style = MaterialTheme.typography.h6)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp)
                .animateContentSize()
        ) {
            if (isExpanded) {
                FlowColumn {
                    repeat(tasks.value.size) { index ->
                        TaskListItem(task = tasks.value[index], navController)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskListItem(task: Task, navController: NavController?) {
    Box(modifier = Modifier
        .padding(top = 5.dp)
        .fillMaxWidth()
        .clickable {
            navController?.navigate("view_task/${task.uid}")
        }) {
        Text(text = task.title)
    }
}*/

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
                println(_dates)
            }
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