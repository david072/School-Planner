package de.david072.schoolplanner.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.preference.PreferenceManager

@Composable
private fun Title(title: @Composable () -> Unit) {
    ProvideTextStyle(value = MaterialTheme.typography.subtitle1) {
        title()
    }
}

@Composable
private fun Subtitle(subtitle: @Composable (() -> Unit)? = null) {
    if (subtitle != null) {
        Spacer(modifier = Modifier.size(2.dp))
        ProvideTextStyle(value = MaterialTheme.typography.caption) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.medium,
                content = subtitle
            )
        }
    }
}

@Composable
private fun Icon(icon: @Composable (() -> Unit)? = null) {
    if (icon != null) {
        Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                icon()
            }
        }
    }
}

@Composable
private fun PreferenceBase(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .let {
                if (onClick != null)
                    it.clickable { onClick() }
                else it
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(15.dp))
        content()
    }
}

@Composable
fun Preference(
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    PreferenceBase(onClick) {
        Icon(icon)

        Column {
            Title(title)
            Subtitle(subtitle)
        }
    }
}

@Composable
fun SelectPreference(
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    items: List<String>,
    key: String? = null
) {
    val context = LocalContext.current

    var dialogVisible by remember { mutableStateOf(false) }
    var subtitleText: String? by remember { mutableStateOf(null) }
    var didInitialize by remember { mutableStateOf(false) }

    if (!didInitialize && subtitle == null) {
        subtitleText = PreferenceManager.getDefaultSharedPreferences(context).getString(key, null)
        didInitialize = true
    }

    PreferenceBase(onClick = { dialogVisible = true }) {
        Icon(icon)

        Column {
            Title(title)
            Subtitle(subtitle ?: subtitleText?.let { { Text(subtitleText!!) } })
        }
    }

    if (dialogVisible) {
        AlertDialog(
            onDismissRequest = { dialogVisible = false },
            title = { title() },
            text = {
                LazyColumn {
                    items(items.size) { index ->
                        val item = items[index]

                        val clickHandler = {
                            if (subtitle == null) subtitleText = item
                            if (key != null) {
                                PreferenceManager.getDefaultSharedPreferences(context).edit {
                                    putString(key, item)
                                }
                            }
                            dialogVisible = false
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { clickHandler() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = false, onClick = clickHandler)
                            Text(item)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogVisible = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            confirmButton = {})
    }
}