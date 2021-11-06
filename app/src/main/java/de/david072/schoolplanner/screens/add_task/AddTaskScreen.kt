package de.david072.schoolplanner.screens.add_task

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.david072.schoolplanner.R
import de.david072.schoolplanner.ui.AppTopAppBar
import de.david072.schoolplanner.ui.theme.SchoolPlannerTheme

@Composable
fun AddTaskScreen(navController: NavController?) {
    Scaffold(topBar = { AppTopAppBar(navController, true) }) {
        Box(modifier = Modifier.padding(PaddingValues(all = 10.dp))) {
            var title by remember { mutableStateOf("") }
            var description by remember { mutableStateOf("") }
            Column(modifier = Modifier.fillMaxHeight()) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 15.dp),
                    maxLines = 1,
                    label = { Text(stringResource(R.string.add_task_title_label)) }
                )

                HorizontalButton(
                    text = stringResource(R.string.add_task_due_date_selector),
                    Icons.Filled.DateRange
                ) { /*TODO*/ }
                CustomSpacer()
                HorizontalButton(
                    text = stringResource(R.string.add_task_reminder_selector),
                    Icons.Filled.Notifications
                ) { /*TODO*/ }
                CustomSpacer()
                HorizontalButton(
                    text = stringResource(R.string.add_task_subject_selector),
                    Icons.Filled.School
                ) { /*TODO*/ }

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                    label = { Text(stringResource(R.string.add_task_description_label)) },
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.add_task_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalButton(
    text: String,
    icon: ImageVector,
    contentDescription: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(top = 15.dp, bottom = 15.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription)
        Text(
            text,
            modifier = Modifier.padding(start = 20.dp),
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@Composable
fun CustomSpacer() {
    Spacer(
        modifier = Modifier
            .background(Color.Gray)
            .height(1.dp)
            .fillMaxWidth()
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(locale = "de")
@Composable
private fun Preview() {
    SchoolPlannerTheme {
        AddTaskScreen(null)
    }
}