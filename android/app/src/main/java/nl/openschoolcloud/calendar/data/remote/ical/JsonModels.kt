package nl.openschoolcloud.calendar.data.remote.ical

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * JSON representation of an attendee for storage in Room
 */
@JsonClass(generateAdapter = true)
data class AttendeeJson(
    val email: String?,
    val name: String?,
    val status: String?,
    val role: String? = null
)

/**
 * JSON representation of a reminder for storage in Room
 */
@JsonClass(generateAdapter = true)
data class ReminderJson(
    val trigger: String,  // e.g., "-PT15M" (ISO 8601 duration)
    val action: String = "DISPLAY"
)

/**
 * JSON serialization utilities for attendees and reminders
 */
object JsonSerializer {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val attendeeListType = Types.newParameterizedType(
        List::class.java,
        AttendeeJson::class.java
    )
    private val attendeeAdapter = moshi.adapter<List<AttendeeJson>>(attendeeListType)

    private val reminderListType = Types.newParameterizedType(
        List::class.java,
        ReminderJson::class.java
    )
    private val reminderAdapter = moshi.adapter<List<ReminderJson>>(reminderListType)

    /**
     * Serialize attendees list to JSON string
     */
    fun serializeAttendees(attendees: List<AttendeeJson>): String {
        return attendeeAdapter.toJson(attendees)
    }

    /**
     * Deserialize JSON string to attendees list
     */
    fun deserializeAttendees(json: String?): List<AttendeeJson> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            attendeeAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Serialize reminders list to JSON string
     */
    fun serializeReminders(reminders: List<ReminderJson>): String {
        return reminderAdapter.toJson(reminders)
    }

    /**
     * Deserialize JSON string to reminders list
     */
    fun deserializeReminders(json: String?): List<ReminderJson> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            reminderAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Parse ISO 8601 duration trigger to minutes before event
     * Handles formats like: -PT15M, -PT1H, -P1D, -PT30M
     */
    fun parseTriggerToMinutes(trigger: String): Int {
        return try {
            val cleanTrigger = trigger.removePrefix("-")
            val duration = java.time.Duration.parse(cleanTrigger)
            duration.toMinutes().toInt().let { if (trigger.startsWith("-")) it else -it }
        } catch (e: Exception) {
            15 // Default to 15 minutes
        }
    }

    /**
     * Convert minutes to ISO 8601 duration trigger
     */
    fun minutesToTrigger(minutes: Int): String {
        return when {
            minutes >= 1440 -> "-P${minutes / 1440}D"
            minutes >= 60 -> "-PT${minutes / 60}H"
            else -> "-PT${minutes}M"
        }
    }
}
