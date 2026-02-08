package nl.openschoolcloud.calendar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.room.Room
import nl.openschoolcloud.calendar.MainActivity
import nl.openschoolcloud.calendar.R
import nl.openschoolcloud.calendar.data.local.AppDatabase
import nl.openschoolcloud.calendar.notification.NotificationHelper
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Builds the RemoteViews for the "Volgende afspraak" (Next Event) widget.
 */
object NextEventWidget {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale("nl"))

    fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        // Query database on a background thread
        Executors.newSingleThreadExecutor().execute {
            val views = buildViews(context, widgetId)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    private fun buildViews(context: Context, widgetId: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_next_event)

        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "openschoolcloud_calendar.db"
        ).build()

        val now = System.currentTimeMillis()
        val endOfTomorrow = Instant.now()
            .plus(2, ChronoUnit.DAYS)
            .toEpochMilli()

        val eventDao = db.eventDao()
        val calendarDao = db.calendarDao()

        val calendars = calendarDao.getVisibleCalendarsSync()
        val colorMap = calendars.associate { it.id to it.colorInt }
        val visibleIds = calendars.map { it.id }.toSet()

        val nextEvent = eventDao.getInRangeSync(now, endOfTomorrow)
            .filter { it.calendarId in visibleIds && it.syncStatus != "PENDING_DELETE" }
            .sortedBy { it.dtStart }
            .firstOrNull()

        if (nextEvent != null) {
            // Show event
            views.setViewVisibility(R.id.widget_next_color, View.VISIBLE)
            views.setViewVisibility(R.id.widget_next_content, View.VISIBLE)
            views.setViewVisibility(R.id.widget_next_empty, View.GONE)

            // Title
            views.setTextViewText(R.id.widget_next_title, nextEvent.summary)

            // Calendar color
            val color = colorMap[nextEvent.calendarId] ?: Color.parseColor("#3B9FD9")
            views.setInt(R.id.widget_next_color, "setBackgroundColor", color)

            // Time - relative or absolute
            val timeText = formatRelativeTime(context, nextEvent.dtStart, nextEvent.dtEnd)
            views.setTextViewText(R.id.widget_next_time, timeText)

            // Location
            if (!nextEvent.location.isNullOrBlank()) {
                views.setTextViewText(R.id.widget_next_location, nextEvent.location)
                views.setViewVisibility(R.id.widget_next_location, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_next_location, View.GONE)
            }

            // Click → open event detail
            val eventIntent = Intent(context, MainActivity::class.java).apply {
                action = NotificationHelper.ACTION_VIEW_EVENT
                putExtra(NotificationHelper.EXTRA_EVENT_ID, nextEvent.uid)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val eventPendingIntent = PendingIntent.getActivity(
                context,
                widgetId + 2000,
                eventIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_next_root, eventPendingIntent)
        } else {
            // Empty state
            views.setViewVisibility(R.id.widget_next_color, View.GONE)
            views.setViewVisibility(R.id.widget_next_content, View.GONE)
            views.setViewVisibility(R.id.widget_next_empty, View.VISIBLE)

            // Click → open calendar
            val calendarIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val calendarPendingIntent = PendingIntent.getActivity(
                context,
                widgetId + 2000,
                calendarIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_next_root, calendarPendingIntent)
        }

        return views
    }

    private fun formatRelativeTime(context: Context, startMillis: Long, endMillis: Long?): String {
        val nowMillis = System.currentTimeMillis()
        val diffMinutes = (startMillis - nowMillis) / 60_000L

        val relativeText = when {
            diffMinutes <= 0 -> context.getString(R.string.widget_now)
            diffMinutes < 60 -> context.getString(R.string.widget_in_minutes, diffMinutes.toInt())
            else -> null
        }

        val zone = ZoneId.systemDefault()
        val startFormatted = Instant.ofEpochMilli(startMillis).atZone(zone).format(timeFormatter)

        return if (relativeText != null) {
            if (endMillis != null) {
                val endFormatted = Instant.ofEpochMilli(endMillis).atZone(zone).format(timeFormatter)
                "$relativeText · $startFormatted - $endFormatted"
            } else {
                "$relativeText · $startFormatted"
            }
        } else {
            if (endMillis != null) {
                val endFormatted = Instant.ofEpochMilli(endMillis).atZone(zone).format(timeFormatter)
                "$startFormatted - $endFormatted"
            } else {
                startFormatted
            }
        }
    }
}
