package de.david072.schoolplanner.screens

import android.app.Application
import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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

@Composable
fun HomeScreen(navController: NavController?) {
    Scaffold(topBar = { AppTopAppBar(navController) }, floatingActionButton = {
        FloatingActionButton(
            onClick = { navController?.navigate("add_task") },
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Icon(Icons.Filled.Add, "")
        }
    }) {
        val viewModel = viewModel<HomeScreenViewModel>()
        Box(modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
            val subjects = viewModel.subjects.collectAsState()
            LazyColumn {
                items(subjects.value.size) { index ->
                    SubjectListItem(subjects.value[index], viewModel, navController)
                }
            }
        }
    }
}

@Composable
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
}

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val _subjects: MutableStateFlow<List<Subject>> = MutableStateFlow(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects

    private val tasks = mutableMapOf<Int, Flow<List<Task>>>().withDefault { emptyFlow() }

    init {
        viewModelScope.launch {
            AppDatabase.instance((getApplication() as Application).applicationContext).subjectDao()
                .getAll().collect {
                    _subjects.value = it
                    it.forEach { subject -> addTasksForSubject(subject.uid) }
                }
        }
    }

    private fun addTasksForSubject(subjectId: Int) {
        if (tasks[subjectId] != null) return
        tasks[subjectId] =
            AppDatabase.instance((getApplication() as Application).applicationContext).taskDao()
                .findBySubject(subjectId)
    }

    fun getTasks(subjectId: Int) = tasks[subjectId]!!
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