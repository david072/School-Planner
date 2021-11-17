package de.david072.schoolplanner.screens.settings

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.david072.schoolplanner.database.AppDatabase
import de.david072.schoolplanner.database.entities.Subject
import de.david072.schoolplanner.ui.AppTopAppBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun EditSubjectsScreen(navController: NavController) {
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
        LazyColumn {
            subjects.value?.let {
                items(it.size) { index ->
                    SubjectListItem(it[index])
                }
            }
        }
    }
}

@Composable
private fun SubjectListItem(subject: Subject) {
    Box(modifier = Modifier.padding(all = 10.dp)) {
        Text(subject.name)
    }
}

class EditSubjectsScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val _subjects: MutableStateFlow<List<Subject>?> = MutableStateFlow(null)
    val subjects: StateFlow<List<Subject>?> = _subjects

    init {
        viewModelScope.launch {
            AppDatabase.instance((getApplication() as Application).applicationContext).subjectDao()
                .getAll().collect {
                    _subjects.value = it
                }
        }
    }
}