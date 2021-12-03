package de.david072.schoolplanner.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import de.david072.schoolplanner.R
import de.david072.schoolplanner.ui.AppTopAppBar
import de.david072.schoolplanner.ui.Preference

@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = { AppTopAppBar(navController = navController, backButton = true) }
    ) {
        Column {
            Preference(
                title = { Text(stringResource(R.string.edit_subjects_setting_title)) },
                icon = { Icon(Icons.Outlined.School, "") },
                onClick = { navController.navigate("settings/subjects") }
            )
            Preference(
                title = { Text("Notifications") },
                icon = { Icon(Icons.Outlined.Notifications, "") },
                onClick = { navController.navigate("settings/notifications") }
            )
        }
    }
}