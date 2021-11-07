package de.david072.schoolplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import de.david072.schoolplanner.screens.HomeScreen
import de.david072.schoolplanner.screens.SubjectSelectorDialog
import de.david072.schoolplanner.screens.add_task.AddTaskScreen
import de.david072.schoolplanner.ui.theme.SchoolPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SchoolPlannerTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home_screen") {
                    composable("home_screen") { HomeScreen(navController) }
                    composable("add_task") { AddTaskScreen(navController) }
                    dialog("subject_select_dialog") { SubjectSelectorDialog(navController) }
                }
            }
        }
    }
}