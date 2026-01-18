package nl.openschoolcloud.calendar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing Nextcloud account information
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val serverUrl: String,
    val username: String,
    val displayName: String?,
    val email: String?,
    val principalUrl: String?,
    val calendarHomeSet: String?,
    val isDefault: Boolean
)
