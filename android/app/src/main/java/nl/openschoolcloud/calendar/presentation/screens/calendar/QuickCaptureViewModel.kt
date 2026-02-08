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
package nl.openschoolcloud.calendar.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.openschoolcloud.calendar.domain.usecase.ParseNaturalLanguageUseCase
import nl.openschoolcloud.calendar.domain.usecase.ParsedEvent
import javax.inject.Inject

@HiltViewModel
class QuickCaptureViewModel @Inject constructor(
    private val parseNaturalLanguageUseCase: ParseNaturalLanguageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickCaptureUiState())
    val uiState: StateFlow<QuickCaptureUiState> = _uiState.asStateFlow()

    private var parseJob: Job? = null

    fun onInputChange(input: String) {
        _uiState.update { it.copy(input = input) }

        parseJob?.cancel()
        parseJob = viewModelScope.launch {
            delay(150) // Debounce 150ms after last keystroke
            if (input.length >= 3) {
                val parsed = parseNaturalLanguageUseCase.parse(input)
                _uiState.update { it.copy(parsedEvent = parsed) }
            } else {
                _uiState.update { it.copy(parsedEvent = null) }
            }
        }
    }

    fun clear() {
        parseJob?.cancel()
        _uiState.update { QuickCaptureUiState() }
    }
}

data class QuickCaptureUiState(
    val input: String = "",
    val parsedEvent: ParsedEvent? = null
)
