package de.david072.schoolplanner.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import de.david072.schoolplanner.R

@Composable
fun AppTopAppBar(
    navController: NavController? = null,
    backButton: Boolean = false,
    onBackPressed: (() -> Unit)? = null
) {
    if (!backButton || navController == null)
        TopAppBar(title = { Text(text = stringResource(R.string.app_name)) })
    else TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
                onBackPressed?.invoke()
            }) { Icon(Icons.Filled.ArrowBack, "") }
        })
}