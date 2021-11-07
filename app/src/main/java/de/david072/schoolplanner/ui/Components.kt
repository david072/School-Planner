package de.david072.schoolplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

@Composable
fun HorizontalSpacer(padding: PaddingValues = PaddingValues(all = 0.dp)) {
    Spacer(
        modifier = Modifier
            .padding(padding)
            .background(Color.Gray)
            .height(1.dp)
            .fillMaxWidth()
    )
}