package nl.openschoolcloud.calendar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for storing calendar events
 */
@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = CalendarEntity::class,
            parentColumns = ["id"],
            childColumns = ["calendarId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("calendarId"),
        Index("dtStart"),
        Index("dtEnd")
    ]
)
data class EventEntity(
    @PrimaryKey
    val uid: String,
    val calendarId: String,
    val summary: String,
    val description: String?,
    val location: String?,
    val dtStart: Long, // epoch millis
    val dtEnd: Long?,
    val allDay: Boolean,
    val timeZone: String,
    val rrule: String?,
    val colorOverride: Int?,
    val organizerEmail: String?,
    val organizerName: String?,
    val attendeesJson: String?, // JSON array of attendees
    val remindersJson: String?, // JSON array of reminders
    val status: String, // CONFIRMED, TENTATIVE, CANCELLED
    val created: Long?,
    val lastModified: Long?,
    val etag: String?,
    val syncStatus: String, // SYNCED, PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE
    val rawIcal: String? // Original iCal data for conflict resolution
)
