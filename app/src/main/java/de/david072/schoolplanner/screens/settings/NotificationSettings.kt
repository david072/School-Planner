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

@Preview
@Composable
fun NotificationSettings(navController: NavController? = null) {
    Scaffold(topBar = { AppTopAppBar(navController = navController, backButton = true) }) {
        Column {
            val notificationHourItems = mutableMapOf<Int, String>()
            for (i in 0 until 24) notificationHourItems[i] = i.toString()

            SelectPreference(
                title = { Text(stringResource(R.string.notification_settings_target_hour_title)) },
                icon = { Icon(Icons.Outlined.Schedule, "") },
                items = notificationHourItems,
                subtitleTemplate = stringResource(R.string.notification_settings_target_hour_subtitle_template),
                key = "notification_target_hour"
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
                key = "notification_priority"
            )
        }
    }
}
