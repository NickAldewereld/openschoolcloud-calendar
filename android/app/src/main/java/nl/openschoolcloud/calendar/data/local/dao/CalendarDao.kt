package nl.openschoolcloud.calendar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import nl.openschoolcloud.calendar.data.local.entity.CalendarEntity

/**
 * Data Access Object for calendar operations
 */
@Dao
interface CalendarDao {

    @Query("SELECT * FROM calendars WHERE accountId = :accountId ORDER BY sortOrder")
    fun getByAccount(accountId: String): Flow<List<CalendarEntity>>

    @Query("SELECT * FROM calendars WHERE visible = 1 ORDER BY sortOrder")
    fun getVisible(): Flow<List<CalendarEntity>>

    @Query("SELECT * FROM calendars WHERE visible = 1 ORDER BY sortOrder")
    suspend fun getVisibleSync(): List<CalendarEntity>

    @Query("SELECT * FROM calendars WHERE visible = 1 ORDER BY sortOrder")
    fun getVisibleCalendarsSync(): List<CalendarEntity>

    @Query("SELECT * FROM calendars")
    fun getAll(): Flow<List<CalendarEntity>>

    @Query("SELECT * FROM calendars")
    suspend fun getAllSync(): List<CalendarEntity>

    @Query("SELECT * FROM calendars WHERE id = :id")
    suspend fun getById(id: String): CalendarEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(calendars: List<CalendarEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calendar: CalendarEntity)

    @Update
    suspend fun update(calendar: CalendarEntity)

    @Query("UPDATE calendars SET ctag = :ctag, syncToken = :syncToken WHERE id = :id")
    suspend fun updateSyncInfo(id: String, ctag: String?, syncToken: String?)

    @Query("DELETE FROM calendars WHERE accountId = :accountId")
    suspend fun deleteByAccount(accountId: String)
}
