package de.david072.schoolplanner.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import de.david072.schoolplanner.R
import de.david072.schoolplanner.ui.AppTopAppBar
import de.david072.schoolplanner.ui.DropdownPreference
import de.david072.schoolplanner.ui.SelectPreference
import de.david072.schoolplanner.util.SettingsKeys

@Preview
@Composable
fun NotificationSettings(navController: NavController? = null) {
    Scaffold(topBar = { AppTopAppBar(navController = navController, backButton = true) }) {
        if (notificationHourItems == null) {
            notificationHourItems = mutableMapOf()
            for (i in 0 until 24) notificationHourItems!![i] = i.toString()
        }

        Column {
            SelectPreference(
                title = { Text(stringResource(R.string.notification_settings_target_hour_title)) },
                icon = { Icon(Icons.Outlined.Schedule, "") },
                items = notificationHourItems!!,
                defaultValue = 12,
                subtitleTemplate = stringResource(R.string.notification_settings_target_hour_subtitle_template),
                key = SettingsKeys.Notifications.notificationTargetHour
            )

            DropdownPreference(
                title = { Text(stringResource(R.string.notification_settings_priority_title)) },
                icon = { Icon(Icons.Outlined.Warning, "") },
                items = mapOf(
                    Pair(
                        NotificationCompat.PRIORITY_HIGH,
                        stringResource(R.string.notification_settings_priority_high)
                    ),
                    Pair(
                        NotificationCompat.PRIORITY_LOW,
                        stringResource(R.string.notification_settings_priority_low)
                    ),
                    Pair(
                        NotificationCompat.PRIORITY_DEFAULT,
                        stringResource(R.string.notification_settings_priority_default)
                    )
                ),
                defaultValue = NotificationCompat.PRIORITY_DEFAULT,
                key = SettingsKeys.Notifications.notificationPriority
            )
        }
    }
}

private var notificationHourItems: MutableMap<Int, String>? = null
