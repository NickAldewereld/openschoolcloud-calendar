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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for JsonSerializer
 */
class JsonSerializerTest {

    // ==================== Attendee serialization tests ====================

    @Test
    fun `serializeAttendees serializes single attendee`() {
        val attendees = listOf(
            AttendeeJson(
                email = "john@example.com",
                name = "John Doe",
                status = "ACCEPTED",
                role = "REQ-PARTICIPANT"
            )
        )

        val json = JsonSerializer.serializeAttendees(attendees)

        assertTrue(json.contains("john@example.com"))
        assertTrue(json.contains("John Doe"))
        assertTrue(json.contains("ACCEPTED"))
    }

    @Test
    fun `serializeAttendees serializes multiple attendees`() {
        val attendees = listOf(
            AttendeeJson("alice@example.com", "Alice", "ACCEPTED", "REQ-PARTICIPANT"),
            AttendeeJson("bob@example.com", "Bob", "TENTATIVE", "OPT-PARTICIPANT"),
            AttendeeJson("charlie@example.com", null, "NEEDS-ACTION", null)
        )

        val json = JsonSerializer.serializeAttendees(attendees)

        assertTrue(json.contains("alice@example.com"))
        assertTrue(json.contains("bob@example.com"))
        assertTrue(json.contains("charlie@example.com"))
    }

    @Test
    fun `serializeAttendees handles empty list`() {
        val json = JsonSerializer.serializeAttendees(emptyList())

        assertEquals("[]", json)
    }

    // ==================== Attendee deserialization tests ====================

    @Test
    fun `deserializeAttendees deserializes valid json`() {
        val json = """[
            {"email":"john@example.com","name":"John Doe","status":"ACCEPTED","role":"REQ-PARTICIPANT"},
            {"email":"jane@example.com","name":"Jane Smith","status":"DECLINED","role":"OPT-PARTICIPANT"}
        ]""".trimIndent()

        val result = JsonSerializer.deserializeAttendees(json)

        assertEquals(2, result.size)
        assertEquals("john@example.com", result[0].email)
        assertEquals("John Doe", result[0].name)
        assertEquals("ACCEPTED", result[0].status)
        assertEquals("jane@example.com", result[1].email)
    }

    @Test
    fun `deserializeAttendees returns empty list for null input`() {
        val result = JsonSerializer.deserializeAttendees(null)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `deserializeAttendees returns empty list for empty string`() {
        val result = JsonSerializer.deserializeAttendees("")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `deserializeAttendees returns empty list for blank string`() {
        val result = JsonSerializer.deserializeAttendees("   ")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `deserializeAttendees returns empty list for invalid json`() {
        val result = JsonSerializer.deserializeAttendees("not valid json")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `deserializeAttendees handles attendees with null fields`() {
        val json = """[
            {"email":"john@example.com","name":null,"status":null,"role":null}
        ]""".trimIndent()

        val result = JsonSerializer.deserializeAttendees(json)

        assertEquals(1, result.size)
        assertEquals("john@example.com", result[0].email)
        assertEquals(null, result[0].name)
        assertEquals(null, result[0].status)
    }

    // ==================== Attendee roundtrip test ====================

    @Test
    fun `attendees survive serialization roundtrip`() {
        val original = listOf(
            AttendeeJson("alice@example.com", "Alice", "ACCEPTED", "REQ-PARTICIPANT"),
            AttendeeJson("bob@example.com", "Bob", "TENTATIVE", null),
            AttendeeJson("charlie@example.com", null, null, null)
        )

        val json = JsonSerializer.serializeAttendees(original)
        val result = JsonSerializer.deserializeAttendees(json)

        assertEquals(original.size, result.size)
        assertEquals(original[0].email, result[0].email)
        assertEquals(original[0].name, result[0].name)
        assertEquals(original[1].email, result[1].email)
        assertEquals(original[2].email, result[2].email)
    }

    // ==================== Reminder serialization tests ====================

    @Test
    fun `serializeReminders serializes single reminder`() {
        val reminders = listOf(
            ReminderJson(trigger = "-PT15M", action = "DISPLAY")
        )

        val json = JsonSerializer.serializeReminders(reminders)

        assertTrue(json.contains("-PT15M"))
        assertTrue(json.contains("DISPLAY"))
    }

    @Test
    fun `serializeReminders serializes multiple reminders`() {
        val reminders = listOf(
            ReminderJson("-PT15M", "DISPLAY"),
            ReminderJson("-PT1H", "DISPLAY"),
            ReminderJson("-P1D", "EMAIL")
        )

        val json = JsonSerializer.serializeReminders(reminders)

        assertTrue(json.contains("-PT15M"))
        assertTrue(json.contains("-PT1H"))
        assertTrue(json.contains("-P1D"))
    }

    @Test
    fun `serializeReminders handles empty list`() {
        val json = JsonSerializer.serializeReminders(emptyList())

        assertEquals("[]", json)
    }

    // ==================== Reminder deserialization tests ====================

    @Test
    fun `deserializeReminders deserializes valid json`() {
        val json = """[
            {"trigger":"-PT15M","action":"DISPLAY"},
            {"trigger":"-PT1H","action":"EMAIL"}
        ]""".trimIndent()

        val result = JsonSerializer.deserializeReminders(json)

        assertEquals(2, result.size)
        assertEquals("-PT15M", result[0].trigger)
        assertEquals("DISPLAY", result[0].action)
        assertEquals("-PT1H", result[1].trigger)
        assertEquals("EMAIL", result[1].action)
    }

    @Test
    fun `deserializeReminders returns empty list for null input`() {
        val result = JsonSerializer.deserializeReminders(null)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `deserializeReminders returns empty list for invalid json`() {
        val result = JsonSerializer.deserializeReminders("invalid")

        assertTrue(result.isEmpty())
    }

    // ==================== Reminder roundtrip test ====================

    @Test
    fun `reminders survive serialization roundtrip`() {
        val original = listOf(
            ReminderJson("-PT15M", "DISPLAY"),
            ReminderJson("-PT1H", "EMAIL"),
            ReminderJson("-P1D", "AUDIO")
        )

        val json = JsonSerializer.serializeReminders(original)
        val result = JsonSerializer.deserializeReminders(json)

        assertEquals(original.size, result.size)
        assertEquals(original[0].trigger, result[0].trigger)
        assertEquals(original[0].action, result[0].action)
        assertEquals(original[1].trigger, result[1].trigger)
        assertEquals(original[2].trigger, result[2].trigger)
    }

    // ==================== parseTriggerToMinutes tests ====================

    @Test
    fun `parseTriggerToMinutes parses minutes correctly`() {
        assertEquals(15, JsonSerializer.parseTriggerToMinutes("-PT15M"))
        assertEquals(30, JsonSerializer.parseTriggerToMinutes("-PT30M"))
        assertEquals(5, JsonSerializer.parseTriggerToMinutes("-PT5M"))
    }

    @Test
    fun `parseTriggerToMinutes parses hours correctly`() {
        assertEquals(60, JsonSerializer.parseTriggerToMinutes("-PT1H"))
        assertEquals(120, JsonSerializer.parseTriggerToMinutes("-PT2H"))
    }

    @Test
    fun `parseTriggerToMinutes parses days correctly`() {
        assertEquals(1440, JsonSerializer.parseTriggerToMinutes("-P1D"))
        assertEquals(2880, JsonSerializer.parseTriggerToMinutes("-P2D"))
    }

    @Test
    fun `parseTriggerToMinutes parses combined durations`() {
        assertEquals(90, JsonSerializer.parseTriggerToMinutes("-PT1H30M"))
    }

    @Test
    fun `parseTriggerToMinutes handles trigger without minus sign`() {
        val result = JsonSerializer.parseTriggerToMinutes("PT15M")
        assertEquals(-15, result)
    }

    @Test
    fun `parseTriggerToMinutes returns default for invalid trigger`() {
        assertEquals(15, JsonSerializer.parseTriggerToMinutes("invalid"))
    }

    @Test
    fun `parseTriggerToMinutes returns default for empty string`() {
        assertEquals(15, JsonSerializer.parseTriggerToMinutes(""))
    }

    // ==================== minutesToTrigger tests ====================

    @Test
    fun `minutesToTrigger creates minutes trigger`() {
        assertEquals("-PT15M", JsonSerializer.minutesToTrigger(15))
        assertEquals("-PT30M", JsonSerializer.minutesToTrigger(30))
        assertEquals("-PT59M", JsonSerializer.minutesToTrigger(59))
    }

    @Test
    fun `minutesToTrigger creates hours trigger`() {
        assertEquals("-PT1H", JsonSerializer.minutesToTrigger(60))
        assertEquals("-PT2H", JsonSerializer.minutesToTrigger(120))
        assertEquals("-PT23H", JsonSerializer.minutesToTrigger(1380))
    }

    @Test
    fun `minutesToTrigger creates days trigger`() {
        assertEquals("-P1D", JsonSerializer.minutesToTrigger(1440))
        assertEquals("-P2D", JsonSerializer.minutesToTrigger(2880))
        assertEquals("-P7D", JsonSerializer.minutesToTrigger(10080))
    }

    // ==================== minutesToTrigger and parseTriggerToMinutes roundtrip ====================

    @Test
    fun `trigger roundtrip for minutes`() {
        val minutes = 15
        val trigger = JsonSerializer.minutesToTrigger(minutes)
        val result = JsonSerializer.parseTriggerToMinutes(trigger)
        assertEquals(minutes, result)
    }

    @Test
    fun `trigger roundtrip for hours`() {
        val minutes = 120 // 2 hours
        val trigger = JsonSerializer.minutesToTrigger(minutes)
        val result = JsonSerializer.parseTriggerToMinutes(trigger)
        assertEquals(minutes, result)
    }

    @Test
    fun `trigger roundtrip for days`() {
        val minutes = 2880 // 2 days
        val trigger = JsonSerializer.minutesToTrigger(minutes)
        val result = JsonSerializer.parseTriggerToMinutes(trigger)
        assertEquals(minutes, result)
    }
}
