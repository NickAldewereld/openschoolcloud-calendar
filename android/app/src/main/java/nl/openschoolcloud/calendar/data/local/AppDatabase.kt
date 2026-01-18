package nl.openschoolcloud.calendar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import nl.openschoolcloud.calendar.data.local.dao.AccountDao
import nl.openschoolcloud.calendar.data.local.dao.CalendarDao
import nl.openschoolcloud.calendar.data.local.dao.EventDao
import nl.openschoolcloud.calendar.data.local.entity.AccountEntity
import nl.openschoolcloud.calendar.data.local.entity.CalendarEntity
import nl.openschoolcloud.calendar.data.local.entity.EventEntity

/**
 * Room database for OpenSchoolCloud Calendar
 */
@Database(
    entities = [
        AccountEntity::class,
        CalendarEntity::class,
        EventEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun calendarDao(): CalendarDao
    abstract fun eventDao(): EventDao
}

/**
 * Type converters for Room database
 * Add converters here as needed for complex types
 */
class Converters {
    // Currently all fields are primitive types or strings
    // Add converters here if needed for complex types like lists
}
