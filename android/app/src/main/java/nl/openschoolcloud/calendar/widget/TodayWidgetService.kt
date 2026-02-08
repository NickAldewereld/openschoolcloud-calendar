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

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.room.Room
import nl.openschoolcloud.calendar.R
import nl.openschoolcloud.calendar.data.local.AppDatabase
import nl.openschoolcloud.calendar.data.local.entity.EventEntity
import nl.openschoolcloud.calendar.notification.NotificationHelper
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * RemoteViewsService that provides the factory for the Today widget's event list.
 */
class TodayWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodayWidgetFactory(applicationContext)
    }
}

/**
 * Factory that loads today's events from the database and provides
 * RemoteViews for each event row in the widget.
 */
class TodayWidgetFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var events: List<WidgetEvent> = emptyList()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale("nl"))

    private val db: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "openschoolcloud_calendar.db"
        ).build()
    }

    data class WidgetEvent(
        val uid: String,
        val summary: String,
        val startTime: Long,
        val calendarColor: Int
    )

    override fun onCreate() {
        // Initial data load happens in onDataSetChanged
    }

    override fun onDataSetChanged() {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val startOfDay = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        // Query events directly from Room (synchronous, runs on binder thread)
        val eventDao = db.eventDao()
        val calendarDao = db.calendarDao()

        // Load calendar colors
        val calendars = calendarDao.getVisibleCalendarsSync()
        val colorMap = calendars.associate { it.id to it.colorInt }
        val visibleCalendarIds = calendars.map { it.id }.toSet()

        // Load events in range
        val rawEvents = eventDao.getInRangeSync(startOfDay, endOfDay)

        events = rawEvents
            .filter { it.calendarId in visibleCalendarIds && it.syncStatus != "PENDING_DELETE" }
            .sortedBy { it.dtStart }
            .take(5)
            .map { entity ->
                WidgetEvent(
                    uid = entity.uid,
                    summary = entity.summary,
                    startTime = entity.dtStart,
                    calendarColor = colorMap[entity.calendarId] ?: Color.parseColor("#3B9FD9")
                )
            }
    }

    override fun onDestroy() {
        events = emptyList()
    }

    override fun getCount(): Int = events.size

    override fun getViewAt(position: Int): RemoteViews {
        val event = events[position]
        val views = RemoteViews(context.packageName, R.layout.widget_event_row)

        // Time
        val time = Instant.ofEpochMilli(event.startTime)
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter)
        views.setTextViewText(R.id.widget_event_time, time)

        // Title
        views.setTextViewText(R.id.widget_event_title, event.summary)

        // Calendar color indicator
        views.setInt(R.id.widget_event_color, "setBackgroundColor", event.calendarColor)

        // Fill-in intent for click handling
        val fillInIntent = Intent().apply {
            putExtra(NotificationHelper.EXTRA_EVENT_ID, event.uid)
        }
        views.setOnClickFillInIntent(R.id.widget_event_row, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = events[position].uid.hashCode().toLong()

    override fun hasStableIds(): Boolean = true
}
