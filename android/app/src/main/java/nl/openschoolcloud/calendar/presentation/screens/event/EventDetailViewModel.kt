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
package nl.openschoolcloud.calendar.presentation.screens.event

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.openschoolcloud.calendar.domain.model.Calendar
import nl.openschoolcloud.calendar.domain.model.Event
import nl.openschoolcloud.calendar.domain.repository.CalendarRepository
import nl.openschoolcloud.calendar.domain.repository.EventRepository
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val eventId: String = savedStateHandle["eventId"] ?: ""

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        loadEvent()
    }

    private fun loadEvent() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val event = eventRepository.getEvent(eventId)
                if (event != null) {
                    val calendar = calendarRepository.getCalendar(event.calendarId)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            event = event,
                            calendar = calendar
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Event niet gevonden")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Onbekende fout")
                }
            }
        }
    }

    fun deleteEvent(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            val result = eventRepository.deleteEvent(eventId)
            result.fold(
                onSuccess = { onDeleted() },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            error = error.message ?: "Verwijderen mislukt"
                        )
                    }
                }
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val event: Event? = null,
    val calendar: Calendar? = null,
    val error: String? = null
)
