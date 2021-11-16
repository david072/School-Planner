package de.david072.schoolplanner.screens.settings

import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.alorma.compose.settings.ui.SettingsMenuLink
import de.david072.schoolplanner.ui.AppTopAppBar

@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = { AppTopAppBar(navController = navController, backButton = true) }
    ) {
        SettingsMenuLink(
            icon = { Icon(Icons.Outlined.School, "") },
            title = { Text("Edit Subjects") },
            onClick = { navController.navigate("settings/edit_subjects") }
        )
    }
}