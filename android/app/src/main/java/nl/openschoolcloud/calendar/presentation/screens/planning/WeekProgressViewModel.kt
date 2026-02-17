/*
 * OSC Calendar - Privacy-first calendar for Dutch education
 * Copyright (C) 2025 Aldewereld Consultancy (OpenSchoolCloud)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 */
package nl.openschoolcloud.calendar.presentation.screens.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.openschoolcloud.calendar.data.local.AppPreferences
import nl.openschoolcloud.calendar.domain.model.Event
import nl.openschoolcloud.calendar.domain.repository.EventRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import javax.inject.Inject

@HiltViewModel
class WeekProgressViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeekProgressUiState())
    val uiState: StateFlow<WeekProgressUiState> = _uiState.asStateFlow()

    private val zoneId: ZoneId = ZoneId.systemDefault()

    init {
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        _uiState.update {
            it.copy(
                weekStart = weekStart,
                planningStreak = appPreferences.planningStreak
            )
        }
        loadTasks()
    }

    private fun loadTasks() {
        val state = _uiState.value
        val weekStart = state.weekStart
        val weekEnd = weekStart.plusDays(7)
        val startInstant = weekStart.atStartOfDay(zoneId).toInstant()
        val endInstant = weekEnd.atStartOfDay(zoneId).toInstant()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            eventRepository.getTasks(startInstant, endInstant)
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { tasks ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tasks = tasks,
                            totalTasks = tasks.size,
                            completedTasks = tasks.count { task -> task.taskCompleted }
                        )
                    }
                }
        }
    }

    fun toggleTaskComplete(taskId: String) {
        viewModelScope.launch {
            eventRepository.toggleTaskCompleted(taskId)
        }
    }

    fun previousWeek() {
        _uiState.update { it.copy(weekStart = it.weekStart.minusWeeks(1)) }
        loadTasks()
    }

    fun nextWeek() {
        _uiState.update { it.copy(weekStart = it.weekStart.plusWeeks(1)) }
        loadTasks()
    }
}

data class WeekProgressUiState(
    val weekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val tasks: List<Event> = emptyList(),
    val planningStreak: Int = 0,
    val isLoading: Boolean = false
) {
    val progress: Float
        get() = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

    val weekNumber: Int
        get() = weekStart.get(WeekFields.ISO.weekOfWeekBasedYear())
}
