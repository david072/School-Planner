package de.david072.schoolplanner.screens.add_task

import android.content.Context
import android.content.res.Configuration
import android.os.Parcel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import de.david072.schoolplanner.R
import de.david072.schoolplanner.database.AppDatabase
import de.david072.schoolplanner.ui.AppTopAppBar
import de.david072.schoolplanner.ui.HorizontalSpacer
import de.david072.schoolplanner.ui.theme.SchoolPlannerTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@Composable
fun AddTaskScreen(navController: NavController?) {
    val context = LocalContext.current

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

                var dueDate: LocalDate? by remember { mutableStateOf(null) }

                HorizontalButton(
                    text = if (dueDate == null) stringResource(R.string.add_task_due_date_selector) else dueDate!!.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                    ),
                    Icons.Filled.DateRange
                ) { pickDate(context) { dueDate = it } }
                HorizontalSpacer()
                HorizontalButton(
                    text = stringResource(R.string.add_task_reminder_selector),
                    Icons.Filled.Notifications
                ) { /*TODO*/ }
                HorizontalSpacer()

                val subjectId = navController?.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getLiveData<Int>("subject_id")
                    ?.observeAsState()

                var subjectText = stringResource(R.string.add_task_subject_selector)
                if (subjectId?.value != null) {
                    val subjectQueryState = AppDatabase.instance(LocalContext.current).subjectDao()
                        .findById(subjectId.value!!).collectAsState(initial = null)
                    if (subjectQueryState.value != null) subjectText =
                        subjectQueryState.value!!.name
                }

                HorizontalButton(
                    text = subjectText,
                    Icons.Filled.School
                ) { navController?.navigate("subject_select_dialog") }

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

private fun pickDate(context: Context, onDateSelected: (LocalDate) -> Unit) {
    MaterialDatePicker.Builder
        .datePicker()
        .setCalendarConstraints(
            CalendarConstraints.Builder()
                .setStart(LocalDate.now().month.value.toLong())
                .setValidator(object : CalendarConstraints.DateValidator {
                    override fun describeContents(): Int = 0
                    override fun writeToParcel(dest: Parcel?, flags: Int) {}

                    // Can only select tomorrow and days after that
                    override fun isValid(date: Long): Boolean =
                        date > Calendar.getInstance().timeInMillis
                }).build()
        )
        .build()
        .apply {
            addOnPositiveButtonClickListener { selection ->
                onDateSelected(
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(selection),
                        TimeZone.getDefault().toZoneId()
                    ).toLocalDate()
                )
            }
            show((context as FragmentActivity).supportFragmentManager, "date-picker")
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

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(locale = "de")
@Composable
private fun Preview() {
    SchoolPlannerTheme {
        AddTaskScreen(null)
    }
}