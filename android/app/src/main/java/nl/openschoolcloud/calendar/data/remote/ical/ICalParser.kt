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

import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.VAlarm
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.parameter.Cn
import net.fortuna.ical4j.model.parameter.PartStat
import net.fortuna.ical4j.model.parameter.Role
import net.fortuna.ical4j.model.property.Attendee
import net.fortuna.ical4j.model.property.RRule
import nl.openschoolcloud.calendar.data.local.entity.EventEntity
import nl.openschoolcloud.calendar.domain.model.EventStatus
import nl.openschoolcloud.calendar.domain.model.SyncStatus
import java.io.StringReader
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser for iCalendar (RFC 5545) data
 *
 * Uses ical4j library to parse iCal strings into EventEntity objects
 * that can be stored in Room database.
 */
@Singleton
class ICalParser @Inject constructor() {

    private val calendarBuilder = CalendarBuilder()

    /**
     * Parse iCal string to EventEntity
     *
     * @param icalData Raw iCal data string
     * @param calendarId Calendar ID this event belongs to
     * @param etag Server etag for sync tracking
     * @return EventEntity or null if parsing fails
     */
    fun parseEvent(
        icalData: String,
        calendarId: String,
        etag: String?
    ): EventEntity? {
        return try {
            val calendar = calendarBuilder.build(StringReader(icalData))
            val vevent = calendar.getComponent<VEvent>(Component.VEVENT)
                ?: return null

            // Required fields
            val uid = vevent.uid?.value ?: return null
            val summary = vevent.summary?.value ?: "Untitled Event"

            // Start time (required)
            val dtStart = vevent.startDate ?: return null
            val startInstant = dtStart.date?.toInstant() ?: return null
            val startMillis = startInstant.toEpochMilli()

            // End time (optional)
            val dtEnd = vevent.endDate
            val endMillis = dtEnd?.date?.toInstant()?.toEpochMilli()

            // All-day detection: VALUE=DATE means all-day event
            val allDay = dtStart.toString().contains("VALUE=DATE") ||
                    (dtStart.date != null && !dtStart.toString().contains("T"))

            // Timezone
            val timeZone = dtStart.timeZone?.id
                ?: ZoneId.systemDefault().id

            // Optional fields
            val description = vevent.description?.value
            val location = vevent.location?.value
            val rrule = vevent.getProperty<RRule>(Property.RRULE)?.value

            // Status
            val status = when (vevent.status?.value?.uppercase()) {
                "TENTATIVE" -> EventStatus.TENTATIVE.name
                "CANCELLED" -> EventStatus.CANCELLED.name
                else -> EventStatus.CONFIRMED.name
            }

            // Organizer
            val organizer = vevent.organizer
            val organizerEmail = organizer?.calAddress?.toString()
                ?.removePrefix("mailto:")
                ?.lowercase()
            val organizerName = organizer?.getParameter<Cn>(Cn.CN)?.value

            // Attendees
            val attendees = vevent.getProperties<Attendee>(Property.ATTENDEE)
            val attendeesJson = if (attendees.isNotEmpty()) {
                serializeAttendees(attendees)
            } else null

            // Reminders/Alarms
            val alarms = vevent.alarms
            val remindersJson = if (alarms.isNotEmpty()) {
                serializeAlarms(alarms)
            } else null

            // Timestamps
            val created = vevent.created?.date?.toInstant()?.toEpochMilli()
            val lastModified = vevent.lastModified?.date?.toInstant()?.toEpochMilli()

            EventEntity(
                uid = uid,
                calendarId = calendarId,
                summary = summary,
                description = description,
                location = location,
                dtStart = startMillis,
                dtEnd = endMillis,
                allDay = allDay,
                timeZone = timeZone,
                rrule = rrule,
                colorOverride = null,
                organizerEmail = organizerEmail,
                organizerName = organizerName,
                attendeesJson = attendeesJson,
                remindersJson = remindersJson,
                status = status,
                created = created,
                lastModified = lastModified,
                etag = etag,
                syncStatus = SyncStatus.SYNCED.name,
                rawIcal = icalData
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse multiple events from a single iCal string
     * (Some calendars return multiple VEVENTs in one response)
     */
    fun parseEvents(
        icalData: String,
        calendarId: String,
        etag: String?
    ): List<EventEntity> {
        return try {
            val calendar = calendarBuilder.build(StringReader(icalData))
            calendar.getComponents<VEvent>(Component.VEVENT).mapNotNull { vevent ->
                parseVEvent(vevent, calendarId, etag, icalData)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Parse a single VEvent component
     */
    private fun parseVEvent(
        vevent: VEvent,
        calendarId: String,
        etag: String?,
        rawIcal: String
    ): EventEntity? {
        return try {
            val uid = vevent.uid?.value ?: return null
            val summary = vevent.summary?.value ?: "Untitled Event"

            val dtStart = vevent.startDate ?: return null
            val startInstant = dtStart.date?.toInstant() ?: return null
            val startMillis = startInstant.toEpochMilli()

            val dtEnd = vevent.endDate
            val endMillis = dtEnd?.date?.toInstant()?.toEpochMilli()

            val allDay = dtStart.toString().contains("VALUE=DATE") ||
                    (dtStart.date != null && !dtStart.toString().contains("T"))

            val timeZone = dtStart.timeZone?.id ?: ZoneId.systemDefault().id

            val status = when (vevent.status?.value?.uppercase()) {
                "TENTATIVE" -> EventStatus.TENTATIVE.name
                "CANCELLED" -> EventStatus.CANCELLED.name
                else -> EventStatus.CONFIRMED.name
            }

            val organizer = vevent.organizer
            val attendees = vevent.getProperties<Attendee>(Property.ATTENDEE)
            val alarms = vevent.alarms

            EventEntity(
                uid = uid,
                calendarId = calendarId,
                summary = summary,
                description = vevent.description?.value,
                location = vevent.location?.value,
                dtStart = startMillis,
                dtEnd = endMillis,
                allDay = allDay,
                timeZone = timeZone,
                rrule = vevent.getProperty<RRule>(Property.RRULE)?.value,
                colorOverride = null,
                organizerEmail = organizer?.calAddress?.toString()?.removePrefix("mailto:")?.lowercase(),
                organizerName = organizer?.getParameter<Cn>(Cn.CN)?.value,
                attendeesJson = if (attendees.isNotEmpty()) serializeAttendees(attendees) else null,
                remindersJson = if (alarms.isNotEmpty()) serializeAlarms(alarms) else null,
                status = status,
                created = vevent.created?.date?.toInstant()?.toEpochMilli(),
                lastModified = vevent.lastModified?.date?.toInstant()?.toEpochMilli(),
                etag = etag,
                syncStatus = SyncStatus.SYNCED.name,
                rawIcal = rawIcal
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Serialize attendees to JSON string
     */
    private fun serializeAttendees(attendees: List<Attendee>): String {
        val list = attendees.mapNotNull { attendee ->
            val email = attendee.calAddress?.toString()?.removePrefix("mailto:")?.lowercase()
            if (email != null) {
                AttendeeJson(
                    email = email,
                    name = attendee.getParameter<Cn>(Cn.CN)?.value,
                    status = attendee.getParameter<PartStat>(PartStat.PARTSTAT)?.value,
                    role = attendee.getParameter<Role>(Role.ROLE)?.value
                )
            } else null
        }
        return JsonSerializer.serializeAttendees(list)
    }

    /**
     * Serialize alarms/reminders to JSON string
     */
    private fun serializeAlarms(alarms: List<VAlarm>): String {
        val list = alarms.mapNotNull { alarm ->
            alarm.trigger?.duration?.toString()?.let { trigger ->
                ReminderJson(
                    trigger = trigger,
                    action = alarm.action?.value ?: "DISPLAY"
                )
            }
        }
        return JsonSerializer.serializeReminders(list)
    }
}
