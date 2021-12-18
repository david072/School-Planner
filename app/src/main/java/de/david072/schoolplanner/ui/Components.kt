package de.david072.schoolplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.david072.schoolplanner.R

@Composable
fun AppTopAppBar(
    navController: NavController? = null,
    backButton: Boolean = false,
    onBackPressed: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    if (!backButton || navController == null)
        TopAppBar(title = { Text(text = stringResource(R.string.app_name)) }, actions = actions)
    else TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
                onBackPressed?.invoke()
            }) { Icon(Icons.Filled.ArrowBack, "") }
        },
        actions = actions
    )
}

@Composable
fun HorizontalButton(
    text: String,
    icon: ImageVector,
    contentDescription: String = "",
    end: @Composable (BoxScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(top = 15.dp, bottom = 15.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription)
        Text(
            text,
            modifier = Modifier.padding(start = 20.dp),
            style = MaterialTheme.typography.subtitle1
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            end?.invoke(this)
            /*Icon(
                Icons.Filled.Error,
                "",
                tint = Color.Red,
                modifier = Modifier.align(Alignment.CenterEnd)
            )*/
        }
    }
}

@Composable
fun HorizontalSpacer(
    padding: PaddingValues = PaddingValues(all = 0.dp),
    background: Color = Color.Gray
) {
    Spacer(
        modifier = Modifier
            .padding(padding)
            .background(background)
            .height(1.dp)
            .fillMaxWidth()
    )
}