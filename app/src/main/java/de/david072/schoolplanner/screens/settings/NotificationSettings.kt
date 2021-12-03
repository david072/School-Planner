package de.david072.schoolplanner.screens.settings

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import de.david072.schoolplanner.ui.AppTopAppBar

@Preview
@Composable
fun NotificationSettings(navController: NavController? = null) {
    Scaffold(topBar = { AppTopAppBar(navController = navController, backButton = true) }) {
        SelectPreference(
            title = { Text("test") },
            items = listOf("Test1", "Test2"),
            key = "notification_target_hour"
        )
    }
}
