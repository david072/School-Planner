package de.david072.schoolplanner.screens

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.david072.schoolplanner.database.AppDatabase
import de.david072.schoolplanner.database.entities.Subject
import de.david072.schoolplanner.ui.HorizontalSpacer

@Composable
fun SubjectSelectorDialog(navController: NavController) {

    val subjects = viewModel<SubjectSelectorViewModel>().getSubjects().observeAsState()

    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text("Select Subject") },
        text = {
            LazyColumn {
                subjects.value?.let {
                    items(it.size) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("subject_id", it[index].uid)
                                    navController.popBackStack()
                                }) {
                            Text(
                                it[index].name,
                                modifier = Modifier
                                    .padding(top = 5.dp, bottom = 5.dp),
                                style = MaterialTheme.typography.body1
                                    .copy(color = MaterialTheme.colors.onBackground)
                            )
                        }
                        if (index < it.size - 1)
                            HorizontalSpacer()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { navController.popBackStack() }) { Text("Cancel") }
        }
    )
}

class SubjectSelectorViewModel(application: Application) : AndroidViewModel(application) {
    private val subjects: MutableLiveData<List<Subject>> by lazy {
        MutableLiveData<List<Subject>>().also {
            AppDatabase.instance(application.applicationContext).subjectDao().getAll()
        }
    }

    fun getSubjects(): LiveData<List<Subject>> = subjects
}