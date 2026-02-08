package nl.openschoolcloud.calendar.presentation.screens.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.openschoolcloud.calendar.domain.model.BookingConfig
import nl.openschoolcloud.calendar.domain.repository.BookingRepository
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        loadBookingConfigs()
    }

    fun loadBookingConfigs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = bookingRepository.getBookingConfigs()
            result.fold(
                onSuccess = { configs ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            configs = configs,
                            isSupported = true
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class BookingUiState(
    val isLoading: Boolean = false,
    val configs: List<BookingConfig> = emptyList(),
    val isSupported: Boolean = true,
    val error: String? = null
)
