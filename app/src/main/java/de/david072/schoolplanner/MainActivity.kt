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
import de.david072.schoolplanner.screens.settings.AddSubjectScreen
import de.david072.schoolplanner.screens.settings.EditSubjectsScreen
import de.david072.schoolplanner.screens.settings.MoveTasksAndDeleteSubjectDialog
import de.david072.schoolplanner.screens.settings.SettingsScreen
import de.david072.schoolplanner.ui.theme.SchoolPlannerTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SchoolPlannerTheme {
                val navController = rememberNavController()

                val taskIdArgument = navArgument("taskId") { type = NavType.IntType }
                val subjectIdArgument = navArgument("subjectId") { type = NavType.IntType }

                NavHost(navController = navController, startDestination = "home_screen") {
                    composable("home_screen") { HomeScreen(navController) }
                    composable("add_task") { AddTaskScreen(navController) }
                    composable("edit_task/{taskId}", arguments = listOf(taskIdArgument)) {
                        AddTaskScreen(navController, it.arguments?.getInt("taskId"))
                    }
                    composable("view_task/{taskId}", arguments = listOf(taskIdArgument)) {
                        ViewTaskScreen(navController, it.arguments!!.getInt("taskId"))
                    }

                    composable("settings") { SettingsScreen(navController) }
                    composable("settings/edit_subjects") { EditSubjectsScreen(navController) }
                    dialog(
                        "settings/edit_subjects/move_tasks_and_delete_subject/{subjectId}",
                        arguments = listOf(subjectIdArgument)
                    ) {
                        MoveTasksAndDeleteSubjectDialog(
                            navController,
                            it.arguments!!.getInt("subjectId")
                        )
                    }

                    composable("settings/add_subject") { AddSubjectScreen(navController) }
                    composable(
                        "settings/edit_subject/{subjectId}",
                        arguments = listOf(subjectIdArgument)
                    ) { AddSubjectScreen(navController, it.arguments!!.getInt("subjectId")) }

                    dialog("subject_select_dialog") { SubjectSelectorDialog(navController) }
                }
            }
        }
    }
}