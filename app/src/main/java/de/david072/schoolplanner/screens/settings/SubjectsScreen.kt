package de.david072.schoolplanner.screens.settings

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.david072.schoolplanner.R
import de.david072.schoolplanner.database.entities.Subject
import de.david072.schoolplanner.database.entities.Task
import de.david072.schoolplanner.database.repositories.SubjectRepository
import de.david072.schoolplanner.database.repositories.TaskRepository
import de.david072.schoolplanner.ui.AppTopAppBar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Composable
fun SubjectsScreen(navController: NavController) {
    Scaffold(
        topBar = { AppTopAppBar(navController = navController, backButton = true) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("settings/add_subject") },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Filled.Add, "")
            }
        }) {
        val viewModel = viewModel<EditSubjectsScreenViewModel>()
        val subjects = viewModel.subjects.collectAsState()
        LazyColumn(modifier = Modifier.padding(all = 10.dp)) {
            subjects.value?.let {
                items(it.size) { index ->
                    SubjectListItem(navController, viewModel, it[index])
                }
            }
        }
    }
}

@Composable
private fun SubjectListItem(
    navController: NavController,
    viewModel: EditSubjectsScreenViewModel,
    subject: Subject
) {
    var confirmDeleteDialogVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(subject.name, modifier = Modifier.padding(start = 10.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                val scope = rememberCoroutineScope()

                IconButton(
                    onClick = {
                        scope.launch {
                            if (viewModel.canDelete(subject.uid)) {
                                confirmDeleteDialogVisible = true
                                return@launch
                            }

                            navController.navigate("settings/edit_subjects/move_tasks_and_delete_subject/${subject.uid}")
                        }
                    },
                ) { Icon(Icons.Outlined.Delete, "") }

                IconButton(
                    onClick = { navController.navigate("settings/edit_subject/${subject.uid}") },
                ) { Icon(Icons.Outlined.Edit, "") }
            }
        }
    }

    if (confirmDeleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { confirmDeleteDialogVisible = false },
            title = { Text(stringResource(R.string.delete_subject_confirmation_dialog_title)) },
            text = {
                Text(
                    stringResource(R.string.delete_subject_confirmation_dialog_message)
                        .replace("%subjectName%", subject.name)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(subject)
                    confirmDeleteDialogVisible = false
                }) { Text(stringResource(R.string.general_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteDialogVisible = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            })
    }
}

class EditSubjectsScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val _subjects: MutableStateFlow<List<Subject>?> = MutableStateFlow(null)
    val subjects: StateFlow<List<Subject>?> = _subjects

    init {
        viewModelScope.launch {
            SubjectRepository(getApplication()).getAll().collect {
                _subjects.value = it
            }
        }
    }

    suspend fun canDelete(subjectId: Int): Boolean =
        TaskRepository(getApplication()).findBySubject(subjectId).first().isEmpty()

    fun delete(subject: Subject) {
        viewModelScope.launch {
            SubjectRepository(getApplication()).delete(subject)
        }
    }
}

// Used to migrate tasks to another subject. This is navigated to
// if a subject should be deleted, but tasks refer to it.
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MoveTasksAndDeleteSubjectDialog(navController: NavController, subjectId: Int) {
    val viewModel = viewModel<MigrateTasksDialogViewModel>()
    viewModel.setSubjectId(subjectId)

    val tasks = viewModel.tasks.collectAsState()
    val subjects = viewModel.subjects.collectAsState()

    var selectedText by remember { mutableStateOf("") }
    var textFieldIsError by remember { mutableStateOf(false) }
    var selectedSubjectId by remember { mutableStateOf(-1) }
    var isExpanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text(stringResource(R.string.move_tasks_title)) },
        text = {
            Column {
                val (focusRequester) = FocusRequester.createRefs()
                val interactionSource = remember { MutableInteractionSource() }

                val tasksSize = tasks.value.size

                Text(stringResource(
                    if (tasksSize > 1) R.string.move_tasks_description_plural
                    else R.string.move_tasks_description_singular
                ).let {
                    if (tasksSize <= 1) return@let it
                    it.replace("%count%", tasksSize.toString())
                })

                Text(
                    stringResource(R.string.move_tasks_move_tasks_option),
                    modifier = Modifier.padding(top = 20.dp)
                )
                Box(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = if (selectedText.isEmpty()) stringResource(R.string.move_tasks_text_field_start_text) else selectedText,
                        onValueChange = {
                            selectedText = it
                            textFieldIsError = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned {
                                textFieldSize = it.size.toSize()
                            }
                            .focusRequester(focusRequester),
                        readOnly = true,
                        isError = textFieldIsError
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                onClick = {
                                    isExpanded = !isExpanded
                                    focusRequester.requestFocus()
                                },
                                interactionSource = interactionSource,
                                indication = null
                            )
                    )
                }
                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                    modifier = Modifier.width(with(
                        LocalDensity.current
                    ) { textFieldSize.width.toDp() })
                ) {
                    subjects.value.forEach {
                        DropdownMenuItem(onClick = {
                            selectedText = it.name
                            selectedSubjectId = it.uid
                            isExpanded = false
                        }) { Text(it.name) }
                    }
                }

                Text(
                    stringResource(R.string.move_tasks_delete_tasks_option),
                    modifier = Modifier.padding(top = 20.dp)
                )
                Button(onClick = {
                    scope.launch {
                        viewModel.deleteTasksAndDeleteSubject()
                        navController.popBackStack()
                    }
                }) {
                    Text(stringResource(
                        if (tasksSize > 1) R.string.move_tasks_delete_tasks_button_plural
                        else R.string.move_tasks_delete_tasks_button_singular
                    ).let {
                        if (tasksSize <= 1) return@let it
                        it.replace("%count%", tasksSize.toString())
                    })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (selectedSubjectId == -1) {
                    textFieldIsError = true
                    return@TextButton
                }

                scope.launch {
                    viewModel.moveTasksAndDeleteSubject(selectedSubjectId)
                    navController.popBackStack()
                }
            }) {
                Text(stringResource(R.string.move_tasks_positive_button))
            }
        },
        dismissButton = {
            TextButton(onClick = { navController.popBackStack() }) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

class MigrateTasksDialogViewModel(application: Application) :
    AndroidViewModel(application) {
    private val _tasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private var subjectId: Int = -1

    private val _subjects: MutableStateFlow<List<Subject>> = MutableStateFlow(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects

    init {
        viewModelScope.launch {
            TaskRepository(getApplication()).findBySubject(subjectId).collect { _tasks.value = it }
        }

        viewModelScope.launch {
            SubjectRepository(getApplication()).getAll().collect {
                val list = ArrayList<Subject>(it)
                val iterator = list.iterator()
                while (iterator.hasNext()) {
                    if (iterator.next().uid == subjectId) {
                        iterator.remove()
                        break
                    }
                }

                _subjects.value = list.toList()
            }
        }
    }

    fun setSubjectId(subjectId: Int) {
        this.subjectId = subjectId

        viewModelScope.launch {
            TaskRepository(getApplication()).findBySubject(subjectId).collect { _tasks.value = it }
        }

        viewModelScope.launch {
            SubjectRepository(getApplication()).getAll().collect {
                val list = ArrayList<Subject>(it)
                val iterator = list.iterator()
                while (iterator.hasNext()) {
                    if (iterator.next().uid == subjectId) {
                        iterator.remove()
                        break
                    }
                }

                _subjects.value = list.toList()
            }
        }
    }

    suspend fun moveTasksAndDeleteSubject(newSubjectId: Int) {
        if (subjectId == -1) throw Exception("subjectId was -1")

        // FIXME: Why tf does this work when debugging but not when actually running it???
        //  Too bad...
        val taskRepository = TaskRepository(getApplication())
        tasks.value.forEach { task ->
            task.subjectId = newSubjectId
            taskRepository.update(task)
        }

        SubjectRepository(getApplication()).delete(subjectId)
    }

    suspend fun deleteTasksAndDeleteSubject() {
        val taskRepository = TaskRepository(getApplication())
        tasks.value.forEach { taskRepository.delete(it) }

        SubjectRepository(getApplication()).delete(subjectId)
    }
}
