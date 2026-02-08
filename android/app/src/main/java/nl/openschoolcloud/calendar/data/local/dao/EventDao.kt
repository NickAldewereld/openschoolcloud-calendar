package nl.openschoolcloud.calendar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import nl.openschoolcloud.calendar.data.local.entity.EventEntity

/**
 * Data Access Object for event operations
 */
@Dao
interface EventDao {

    @Query("""
        SELECT * FROM events
        WHERE dtStart < :endMillis AND (dtEnd > :startMillis OR dtEnd IS NULL)
        AND calendarId IN (SELECT id FROM calendars WHERE visible = 1)
        ORDER BY dtStart
    """)
    fun getInRange(startMillis: Long, endMillis: Long): Flow<List<EventEntity>>

    @Query("""
        SELECT * FROM events
        WHERE dtStart < :endMillis AND (dtEnd > :startMillis OR dtEnd IS NULL)
        ORDER BY dtStart
    """)
    fun getInRangeSync(startMillis: Long, endMillis: Long): List<EventEntity>

    @Query("SELECT * FROM events WHERE uid = :uid")
    suspend fun getByUid(uid: String): EventEntity?

    @Query("SELECT * FROM events WHERE calendarId = :calendarId")
    fun getByCalendar(calendarId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE calendarId = :calendarId")
    suspend fun getByCalendarSync(calendarId: String): List<EventEntity>

    @Query("SELECT * FROM events WHERE syncStatus != 'SYNCED'")
    suspend fun getPending(): List<EventEntity>

    @Query("""
        SELECT * FROM events
        WHERE summary LIKE '%' || :query || '%'
           OR location LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY dtStart DESC
        LIMIT 50
    """)
    fun search(query: String): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>)

    @Update
    suspend fun update(event: EventEntity)

    @Query("DELETE FROM events WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)

    @Query("DELETE FROM events WHERE calendarId = :calendarId")
    suspend fun deleteByCalendar(calendarId: String)
}
