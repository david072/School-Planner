package de.david072.schoolplanner.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.work.*
import de.david072.schoolplanner.MainActivity
import de.david072.schoolplanner.R
import de.david072.schoolplanner.Utils
import de.david072.schoolplanner.database.SubjectRepository
import de.david072.schoolplanner.database.TaskRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val taskRepository = TaskRepository(context)
        val subjectRepository = SubjectRepository(context)

        val tasks = taskRepository.getAll().first()
        val now = LocalDate.now()

        tasks.forEach { task ->
            val epochDay = task.dueDate.toEpochDay()
            // Delete the task if the due date passed
            if (epochDay < LocalDate.now().toEpochDay()) {
                taskRepository.delete(task)
                return@forEach
            }

            if (!task.completed && (task.reminder.isBefore(now) || task.reminder.isEqual(now))) {
                val subject = subjectRepository.findById(task.subjectId).first()

                val contentText = context.getString(R.string.notification_task_due_content_text)
                    .replace("%taskTitle%", task.title)
                    .replace("%subjectName%", subject.name)
                    .replace(
                        "%dueDateInfo%",
                        Utils.formattedDate(task.dueDate, context, withPreposition = true)
                    )

                if (channelId.isEmpty()) createNotificationChannel(context)

                val sharedPrefs = context.getSharedPreferences("MainActivity", Context.MODE_PRIVATE)
                val notificationId = sharedPrefs.getInt(LAST_NOTIFICATION_KEY, 0) + 1

                println("Notification id: $notificationId")

                val markCompletedIntent = Intent(context, MarkCompletedReceiver::class.java).apply {
                    putExtra(MarkCompletedReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                    putExtra(MarkCompletedReceiver.EXTRA_TASK_ID, task.uid)
                }

                val markCompletedPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    markCompletedIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                // TODO: Add: On click to go to view task screen, action to mark the task completed
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("${subject.name}: ${task.title}")
                    .setContentText(contentText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                    .addAction(
                        R.drawable.ic_check,
                        context.getString(R.string.notification_mark_completed_action),
                        markCompletedPendingIntent
                    )
                    .setContentIntent(
                        PendingIntent.getActivity(
                            context,
                            0,
                            Intent(context, MainActivity::class.java).apply {
                                putExtra(
                                    MainActivity.NAV_START_ROUTE_KEY,
                                    "view_task/${task.uid}"
                                )
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            },
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .apply {
                        // TODO: Make this a setting too?
                        priority = NotificationCompat.PRIORITY_HIGH
                    }
                    .build()

                NotificationManagerCompat.from(context).notify(notificationId, notification)
                sharedPrefs.edit { putInt(LAST_NOTIFICATION_KEY, notificationId) }
            }
        }

        enqueueWork(context)
        return Result.success()
    }

    companion object {
        private const val TAG = "notification-worker-tag"
        private const val LAST_NOTIFICATION_KEY = "notification-worker-last-notification-key"
        private var channelId = "" // Notification channel id

        fun ensureReady(context: Context) {
            createNotificationChannel(context)
            ensureStarted(context)
        }

        private fun ensureStarted(context: Context) {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosByTag(TAG)
                .get(15, TimeUnit.SECONDS)

            if (workInfos.isEmpty()) {
                enqueueWork(context)
                return
            }

            val workInfo = workInfos[0]
            if (workInfo.state == WorkInfo.State.BLOCKED || workInfo.state == WorkInfo.State.CANCELLED)
                enqueueWork(context)
        }

        private fun enqueueWork(context: Context) {
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance()

            // Sets execution at around 12:00 AM
            // TODO: Make this a setting
            dueDate.set(Calendar.HOUR_OF_DAY, 12)
            dueDate.set(Calendar.MINUTE, 0)
            dueDate.set(Calendar.SECOND, 0)
            if (dueDate.before(currentDate))
                dueDate.add(Calendar.HOUR_OF_DAY, 24)

            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }

        private fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )

            getNotificationManager(context).createNotificationChannel(channel)
        }

        private fun getNotificationManager(context: Context) =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}