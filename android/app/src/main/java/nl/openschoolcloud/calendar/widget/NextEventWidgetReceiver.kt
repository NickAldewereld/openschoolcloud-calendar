package nl.openschoolcloud.calendar.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * AppWidgetProvider for the "Volgende afspraak" (Next Event) home screen widget.
 * Shows the next upcoming event with title, time, and location.
 */
class NextEventWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            NextEventWidget.updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // First widget placed
    }

    override fun onDisabled(context: Context) {
        // Last widget removed
    }

    companion object {
        /**
         * Trigger a refresh of all Next Event widgets.
         * Call this after event create/edit/delete.
         */
        fun refreshAll(context: Context) {
            val intent = Intent(context, NextEventWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, NextEventWidgetReceiver::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
