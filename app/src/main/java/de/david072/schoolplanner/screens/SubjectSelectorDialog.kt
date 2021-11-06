package de.david072.schoolplanner.screens

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun SubjectSelectorDialog() {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Select Subject") },
        text = { Text("Subjects...") },
        confirmButton = {
            TextButton(onClick = { /*TODO*/ }) { Text("Ok") }
        },
        dismissButton = {
            TextButton(onClick = { /*TODO*/ }) { Text("Cancel") }
        }
    )
}