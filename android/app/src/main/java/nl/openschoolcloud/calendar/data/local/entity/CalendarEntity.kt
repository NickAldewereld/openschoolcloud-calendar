package nl.openschoolcloud.calendar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for storing calendar information
 */
@Entity(
    tableName = "calendars",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountId")]
)
data class CalendarEntity(
    @PrimaryKey
    val id: String,
    val accountId: String,
    val displayName: String,
    @ColumnInfo(name = "color")
    val colorInt: Int,
    val url: String,
    val ctag: String?,
    val syncToken: String?,
    val readOnly: Boolean,
    val visible: Boolean,
    val sortOrder: Int
)
