package de.david072.schoolplanner.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.*
import de.david072.schoolplanner.MainActivity
import de.david072.schoolplanner.R
import de.david072.schoolplanner.database.repositories.ExamRepository
import de.david072.schoolplanner.database.repositories.SubjectRepository
import de.david072.schoolplanner.database.repositories.TaskRepository
import de.david072.schoolplanner.util.SettingsKeys
import de.david072.schoolplanner.util.Utils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    private var notificationId: Int? = null

    override suspend fun doWork(): Result = coroutineScope {
        val subjectRepository = SubjectRepository(context)

        val sharedPrefs = context.getSharedPreferences("MainActivity", Context.MODE_PRIVATE)
        notificationId = sharedPrefs.getInt(LAST_NOTIFICATION_KEY, 0)

        val taskTask = async { sendTaskNotifications(subjectRepository) }
        val examTask = async { sendExamNotifications(subjectRepository) }

        awaitAll(taskTask, examTask)

        sharedPrefs.edit {
            putInt(LAST_NOTIFICATION_KEY, notificationId!!)
        }

        enqueueWork(context)
        return@coroutineScope Result.success()
    }

    private suspend fun sendTaskNotifications(subjectRepository: SubjectRepository) {
        if (notificationId == null) return

        val now = LocalDate.now()
        val taskRepository = TaskRepository(context)
        val tasks = taskRepository.getAll().first()

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

                // Whyyyyyyy
                notificationId = notificationId!! + 1

                sendNotification(
                    notificationId!!,
                    "${subject.name}: ${task.title}",
                    contentText,
                    task.uid,
                    "view_task/${task.uid}",
                    true
                )
            }
        }
    }

    private suspend fun sendExamNotifications(subjectRepository: SubjectRepository) {
        if (notificationId == null) return

        val now = LocalDate.now()
        val examRepository = ExamRepository(context)
        val exams = examRepository.getAll().first()

        exams.forEach { exam ->
            val epochDay = exam.dueDate.toEpochDay()
            // Delete the exam if the due date passed
            if (epochDay < LocalDate.now().toEpochDay()) {
                examRepository.delete(exam)
                return@forEach
            }

            if (exam.reminder.isBefore(now) || exam.reminder.isEqual(now)) {
                val subject = subjectRepository.findById(exam.subjectId).first()

                val contentText = context.getString(R.string.notification_exam_due_content_text)
                    .replace("%examTitle%", exam.title)
                    .replace("%subjectName%", subject.name)
                    .replace(
                        "%dueDateInfo%",
                        Utils.formattedDate(exam.dueDate, context, withPreposition = true)
                    )

                // Whyyyyyyy
                notificationId = notificationId!! + 1

                sendNotification(
                    notificationId!!,
                    "${subject.name}: ${exam.title}",
                    contentText,
                    exam.uid,
                    "view_test/${exam.uid}",
                    false
                )
            }
        }
    }

    private fun sendNotification(
        notificationId: Int,
        contentTitle: String,
        contentText: String,
        uid: Int,
        route: String,
        isCompletable: Boolean
    ) {
        if (channelId.isEmpty()) createNotificationChannel(context)

        val markCompletedIntent = Intent(context, MarkCompletedReceiver::class.java).apply {
            putExtra(MarkCompletedReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(MarkCompletedReceiver.EXTRA_TASK_ID, uid)
        }

        val markCompletedPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            markCompletedIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .run {
                if (isCompletable) {
                    addAction(
                        R.drawable.ic_check,
                        context.getString(R.string.notification_mark_completed_action),
                        markCompletedPendingIntent
                    )
                } else this
            }
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java).apply {
                        putExtra(
                            MainActivity.NAV_START_ROUTE_KEY,
                            route
                        )
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .apply {
                priority = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                    SettingsKeys.Notifications.notificationPriority,
                    NotificationCompat.PRIORITY_DEFAULT
                )
            }
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
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

            // Sets execution at what was set in the settings by the user, or 12:00 by default
            val hourOfDay = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(SettingsKeys.Notifications.notificationTargetHour, 12)

            dueDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
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