package de.david072.schoolplanner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.david072.schoolplanner.screens.AddTaskScreen
import de.david072.schoolplanner.screens.HomeScreen
import de.david072.schoolplanner.screens.SubjectSelectorDialog
import de.david072.schoolplanner.screens.ViewTaskScreen
import de.david072.schoolplanner.ui.theme.SchoolPlannerTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SchoolPlannerTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home_screen") {
                    composable("home_screen") { HomeScreen(navController) }
                    composable("add_task") { AddTaskScreen(navController) }
                    composable(
                        "view_task/{taskId}",
                        arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        ViewTaskScreen(
                            navController,
                            backStackEntry.arguments!!.getInt("taskId")
                        )
                    }
                    dialog("subject_select_dialog") { SubjectSelectorDialog(navController) }
                }
            }
        }
    }
}