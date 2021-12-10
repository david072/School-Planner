package de.david072.schoolplanner.workers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import de.david072.schoolplanner.database.repositories.TaskRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MarkCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)

        if (notificationId == -1 || taskId == -1) {
            Log.e(
                "MarkCompletedReceiver",
                "notification id or task id was not present! (notificationId: $notificationId, taskId: $taskId)"
            )
            return
        }

        // Remove notification
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(notificationId)

        GlobalScope.launch {
            val taskRepository = TaskRepository(context)
            val task = taskRepository.findById(taskId).first()

            // noop
            if (task.completed) return@launch
            taskRepository.update(task.apply { completed = true })
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "task-id"
        const val EXTRA_NOTIFICATION_ID = "notification-id"
    }

}