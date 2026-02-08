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
