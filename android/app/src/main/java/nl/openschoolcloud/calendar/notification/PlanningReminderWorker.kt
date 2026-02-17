/*
 * OSC Calendar - Privacy-first calendar for Dutch education
 * Copyright (C) 2025 Aldewereld Consultancy (OpenSchoolCloud)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 */
package nl.openschoolcloud.calendar.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import nl.openschoolcloud.calendar.data.local.AppPreferences
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that runs daily and shows a planning reminder
 * on the configured day of the week.
 */
@HiltWorker
class PlanningReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val appPreferences: AppPreferences
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val WORK_NAME = "planning_reminder"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PlanningReminderWorker>(
                1, TimeUnit.DAYS,
                2, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        if (!appPreferences.weekPlanningNotificationsEnabled) return Result.success()

        val today = LocalDate.now()
        val targetDay = DayOfWeek.of(appPreferences.planningDayOfWeek)

        if (today.dayOfWeek == targetDay) {
            notificationHelper.showPlanningReminder()
        }

        return Result.success()
    }
}
