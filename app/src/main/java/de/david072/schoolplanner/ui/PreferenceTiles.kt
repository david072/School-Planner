package de.david072.schoolplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
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
private fun PreferenceBase(
    onClick: (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
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
        if (icon == null)
            Box(modifier = Modifier.width(15.dp))
        else Icon(icon)
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
    PreferenceBase(onClick, icon) {
        Column {
            Title(title)
            Subtitle(subtitle)
        }
    }
}

// Dialog looks weird when there are a lot of items displayed
@Composable
fun SelectPreference(
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    subtitleTemplate: String? = null,
    icon: @Composable (() -> Unit)? = null,
    items: List<String>,
    key: String? = null
) {
    val context = LocalContext.current

    var dialogVisible by remember { mutableStateOf(false) }
    var selectedOption: String? by remember { mutableStateOf(null) }
    var didInitialize by remember { mutableStateOf(false) }

    if (!didInitialize && subtitle == null) {
        selectedOption = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(key, null)
        didInitialize = true
    }

    PreferenceBase(onClick = { dialogVisible = true }, icon) {
        Column {
            Title(title)
            Subtitle(subtitle ?: selectedOption?.let {
                {
                    if (subtitleTemplate != null && subtitleTemplate.contains("%val%"))
                        Text(subtitleTemplate.replace("%val%", selectedOption!!))
                    else Text(selectedOption!!)
                }
            })
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
                            if (subtitle == null) selectedOption = item
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

// TODO: Add ripple effect on click
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun DropdownPreference(
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    subtitleTemplate: String? = null,
    icon: @Composable (() -> Unit)? = null,
    items: List<String>,
    key: String? = null
) {
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }
    var selectedOption: String? by remember { mutableStateOf(null) }
    var didInitialize by remember { mutableStateOf(false) }

    if (!didInitialize && subtitle == null) {
        selectedOption = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(key, null)
        didInitialize = true
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        PreferenceBase(icon = icon) {
            Column {
                Title(title)
                Subtitle(subtitle ?: selectedOption?.let {
                    {
                        if (subtitleTemplate != null && subtitleTemplate.contains("%val%"))
                            Text(subtitleTemplate.replace("%val%", selectedOption!!))
                        else Text(selectedOption!!)
                    }
                })
            }
        }

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(onClick = {
                    if (subtitle == null) selectedOption = item
                    if (key != null) {
                        PreferenceManager.getDefaultSharedPreferences(context).edit {
                            putString(key, item)
                        }
                    }
                    expanded = false
                }) {
                    Text(item)
                }
            }
        }
    }
}