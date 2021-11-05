package de.david072.schoolplanner.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import de.david072.schoolplanner.ui.AppTopAppBar

@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(topBar = { AppTopAppBar(navController) }, floatingActionButton = {
        FloatingActionButton(onClick = { navController.navigate("add_task") }, backgroundColor = MaterialTheme.colors.primary) {
            Icon(Icons.Filled.Add, "")
        }
    }) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {

        }
    }
}