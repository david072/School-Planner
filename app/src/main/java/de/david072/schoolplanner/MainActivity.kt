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
import de.david072.schoolplanner.screens.settings.*
import de.david072.schoolplanner.ui.theme.SchoolPlannerTheme
import de.david072.schoolplanner.workers.NotificationWorker

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SchoolPlannerTheme {
                val navController = rememberNavController()

                val taskIdArgument = navArgument("taskId") { type = NavType.IntType }
                val testIdArgument = navArgument("testId") { type = NavType.IntType }
                val subjectIdArgument = navArgument("subjectId") { type = NavType.IntType }

                NavHost(navController = navController, startDestination = "home_screen") {
                    composable("home_screen") { HomeScreen(navController) }
                    composable("add_task") { AddTaskScreen(navController, false) }
                    composable("add_test") { AddTaskScreen(navController, true) }
                    composable("edit_task/{taskId}", arguments = listOf(taskIdArgument)) {
                        AddTaskScreen(navController, false, it.arguments?.getInt("taskId"))
                    }
                    composable("edit_test/{testId}", arguments = listOf(testIdArgument)) {
                        AddTaskScreen(navController, true, it.arguments?.getInt("testId"))
                    }
                    composable("view_task/{taskId}", arguments = listOf(taskIdArgument)) {
                        ViewTaskScreen(navController, it.arguments!!.getInt("taskId"))
                    }
                    composable("view_test/{testId}", arguments = listOf(testIdArgument)) {
                        ViewTaskScreen(navController, it.arguments!!.getInt("testId"), true)
                    }

                    composable("settings") { SettingsScreen(navController) }
                    composable("settings/subjects") { SubjectsScreen(navController) }
                    dialog(
                        "settings/edit_subjects/move_tasks_and_delete_subject/{subjectId}",
                        arguments = listOf(subjectIdArgument)
                    ) {
                        MoveTasksAndDeleteSubjectDialog(
                            navController,
                            it.arguments!!.getInt("subjectId")
                        )
                    }

                    composable("settings/notifications") { NotificationSettings(navController) }

                    composable("settings/add_subject") { AddSubjectScreen(navController) }
                    composable(
                        "settings/edit_subject/{subjectId}",
                        arguments = listOf(subjectIdArgument)
                    ) { AddSubjectScreen(navController, it.arguments!!.getInt("subjectId")) }

                    dialog("subject_select_dialog?id={id}", arguments = listOf(navArgument("id") {
                        type = NavType.IntType
                        defaultValue = -1
                    })) {
                        val idArgument = it.arguments?.getInt("id")!!
                        SubjectSelectorDialog(
                            navController,
                            if (idArgument == -1) null else idArgument
                        )
                    }
                }

                val startDestination = intent.getStringExtra(NAV_START_ROUTE_KEY)
                if (startDestination != null)
                    navController.navigate(startDestination)
            }
        }

        // FIXME: Do this only once per app start
        NotificationWorker.ensureReady(this)
    }

    companion object {
        const val NAV_START_ROUTE_KEY = "main-nav-start-route"
    }
}