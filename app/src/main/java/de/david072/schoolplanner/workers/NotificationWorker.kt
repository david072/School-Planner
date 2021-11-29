package de.david072.schoolplanner.workers

import android.content.Context
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        enqueueWork(context)
        return Result.success()
    }

    companion object {
        private const val TAG = "notification-worker-tag"

        fun ensureStarted(context: Context) {
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
    }
}