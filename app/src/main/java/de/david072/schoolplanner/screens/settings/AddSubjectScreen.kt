package de.david072.schoolplanner.screens.settings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import de.david072.schoolplanner.R
import de.david072.schoolplanner.database.entities.Subject
import de.david072.schoolplanner.database.repositories.SubjectRepository
import de.david072.schoolplanner.ui.AppTopAppBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalGraphicsApi::class)
@Composable
fun AddSubjectScreen(navController: NavController, subjectIdToEdit: Int? = null) {
    val viewModel = viewModel<AddSubjectScreenViewModel>()

    if (subjectIdToEdit != null) viewModel.setSubjectId(subjectIdToEdit)
    val subjectToEdit = viewModel.subject.collectAsState()

    Scaffold(topBar = { AppTopAppBar(navController = navController, backButton = true) }) {
        var didSetValues by remember { mutableStateOf(false) }

        var name by remember { mutableStateOf("") }
        var nameIsError by remember { mutableStateOf(false) }
        var abbreviation by remember { mutableStateOf("") }
        var abbreviationIsError by remember { mutableStateOf(false) }

        var colorPickerVisible by remember { mutableStateOf(false) }
        var color by remember {
            val random = Random()
            mutableStateOf(
                Color(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
                )
            )
        }

        if (subjectToEdit.value != null && !didSetValues) {
            name = subjectToEdit.value!!.name
            abbreviation = subjectToEdit.value!!.abbreviation
            color = subjectToEdit.value!!.color()
            didSetValues = true
        }

        fun validate(): Boolean {
            var isValid = true
            if (name.apply { trim() }.isEmpty()) {
                isValid = false
                nameIsError = true
            }
            if (abbreviation.apply { trim() }.isEmpty()) {
                isValid = false
                abbreviationIsError = true
            }

            return isValid
        }

        Column(modifier = Modifier.padding(all = 10.dp)) {
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                maxLines = 1,
                label = { Text(stringResource(R.string.add_subject_name_label)) },
                isError = nameIsError
            )

            // FIXME: Should have both children as the same height...
            //  TextField looks smaller though???
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                TextField(
                    value = abbreviation,
                    onValueChange = { abbreviation = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 15.dp, end = 5.dp),
                    maxLines = 1,
                    label = { Text(stringResource(R.string.add_subject_abbreviation_label)) },
                    isError = abbreviationIsError
                )

                Box(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(5.dp))
                        .background(color)
                        .clickable {
                            colorPickerVisible = true
                        }) {
                    Text(
                        stringResource(R.string.add_subject_color_label),
                        modifier = Modifier.align(Alignment.Center),
                        color = if (HsvColor.from(color).value > .5) Color.Black else Color.White
                    )
                }
            }

            Box(modifier = Modifier.fillMaxHeight()) {
                Button(modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(), onClick = {
                    if (!validate()) return@Button
                    Subject(
                        uid = if (subjectToEdit.value != null) subjectToEdit.value!!.uid else 0,
                        name = name,
                        abbreviation = abbreviation,
                        colorValue = color.toArgb()
                    ).let {
                        if (subjectIdToEdit == null)
                            viewModel.insert(it)
                        else viewModel.update(it)
                    }
                    navController.popBackStack()
                }) {
                    Text(
                        stringResource(
                            if (subjectIdToEdit == null) R.string.add_subject_button
                            else R.string.general_save
                        )
                    )
                }
            }
        }

        if (colorPickerVisible) {
            ColorPickerDialog(color) {
                colorPickerVisible = false
                if (it != null)
                    color = it.toColor()
            }
        }
    }
}

@ExperimentalGraphicsApi
@Composable
private fun ColorPickerDialog(
    startColor: Color,
    onCloseRequest: (selectedColor: HsvColor?) -> Unit
) {
    var color by remember { mutableStateOf(HsvColor.from(startColor)) }
    AlertDialog(
        onDismissRequest = { onCloseRequest(null) },
        title = { Text(stringResource(R.string.add_subject_dialog_title)) },
        text = {
            ClassicColorPicker(
                modifier = Modifier.height(400.dp),
                showAlphaBar = false,
                color = startColor,
                onColorChanged = { color = it })
        },
        confirmButton = {
            TextButton(onClick = {
                onCloseRequest(color)
            }) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = { onCloseRequest(null) }) {
                Text(stringResource(android.R.string.cancel))
            }
        })
}

class AddSubjectScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val _subjectToEdit: MutableStateFlow<Subject?> = MutableStateFlow(null)
    val subject: StateFlow<Subject?> = _subjectToEdit

    fun setSubjectId(subjectId: Int) {
        viewModelScope.launch {
            SubjectRepository(getApplication()).findById(subjectId).collect {
                _subjectToEdit.value = it
            }
        }
    }

    fun insert(subject: Subject) {
        viewModelScope.launch {
            SubjectRepository(getApplication()).insert(subject)
        }
    }

    fun update(subject: Subject) {
        viewModelScope.launch {
            SubjectRepository(getApplication()).update(subject)
        }
    }
}