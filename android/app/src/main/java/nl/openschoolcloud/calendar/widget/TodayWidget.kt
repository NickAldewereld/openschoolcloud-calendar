/*
 * OpenSchoolCloud Calendar
 * Copyright (C) 2025 OpenSchoolCloud / Aldewereld Consultancy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package nl.openschoolcloud.calendar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import nl.openschoolcloud.calendar.MainActivity
import nl.openschoolcloud.calendar.R
import nl.openschoolcloud.calendar.notification.NotificationHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Builds the RemoteViews for the "Vandaag" widget.
 */
object TodayWidget {

    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale("nl"))

    fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_today)

        // Set today's date
        val today = LocalDate.now()
        val dateText = today.format(dateFormatter).replaceFirstChar { it.uppercase() }
        views.setTextViewText(R.id.widget_date, dateText)

        // Header click → open CalendarScreen
        val calendarIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val calendarPendingIntent = PendingIntent.getActivity(
            context,
            widgetId,
            calendarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_header, calendarPendingIntent)

        // Set up the ListView with RemoteViewsService
        val serviceIntent = Intent(context, TodayWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        views.setRemoteAdapter(R.id.widget_event_list, serviceIntent)
        views.setEmptyView(R.id.widget_event_list, R.id.widget_empty)

        // Template for event item clicks → open EventDetailScreen
        val eventClickIntent = Intent(context, MainActivity::class.java).apply {
            action = NotificationHelper.ACTION_VIEW_EVENT
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val eventClickPendingIntent = PendingIntent.getActivity(
            context,
            widgetId + 1000,
            eventClickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setPendingIntentTemplate(R.id.widget_event_list, eventClickPendingIntent)

        // Notify the widget manager to update
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_event_list)
        appWidgetManager.updateAppWidget(widgetId, views)
    }
}
