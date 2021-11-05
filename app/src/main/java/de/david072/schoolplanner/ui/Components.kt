package de.david072.schoolplanner.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import de.david072.schoolplanner.R

@Composable
fun AppTopAppBar(
    navController: NavController,
    backButton: Boolean = false,
    onBackPressed: (() -> Unit)? = null
) {
    val context = LocalContext.current

    if (!backButton)
        TopAppBar(title = { Text(text = context.resources.getString(R.string.app_name)) })
    else TopAppBar(
        title = { Text(text = context.resources.getString(R.string.app_name)) },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
                onBackPressed?.invoke()
            }) { Icon(Icons.Filled.ArrowBack, "") }
        })
}