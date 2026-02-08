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
