package de.david072.schoolplanner.screens.settings

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
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
        LazyColumn(modifier = Modifier.padding(all = 10.dp)) {
            subjects.value?.let {
                items(it.size) { index ->
                    SubjectListItem(navController, it[index])
                }
            }
        }
    }
}

@Composable
private fun SubjectListItem(navController: NavController, subject: Subject) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(subject.name, modifier = Modifier.padding(start = 10.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { navController.navigate("settings/edit_subject/${subject.uid}") },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) { Icon(Icons.Outlined.Edit, "") }
        }
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