package de.david072.schoolplanner.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.david072.schoolplanner.database.SubjectRepository
import de.david072.schoolplanner.ui.HorizontalSpacer

@Composable
fun SubjectSelectorDialog(navController: NavController, id: Int?) {
    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text("Select Subject") },
        text = {
            val subjects = SubjectRepository(LocalContext.current).getAll()
                .collectAsState(initial = listOf())

            LazyColumn {
                subjects.value.let {
                    items(it.size) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set(
                                            // Append the id with a leading underscore if we have one
                                            "subject_id${if (id != null) "_$id" else ""}",
                                            it[index].uid
                                        )
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