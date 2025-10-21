package com.example.healthcaredispenser.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcaredispenser.data.model.intake.CreateIntakeRequest
import com.example.healthcaredispenser.data.repository.IntakeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = false,
    val intakeId: Long? = null,
    val status: String? = null,
    val error: String? = null
)

class HomeViewModel(
    private val repo: IntakeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    fun requestIntake(profileId: Long, dispenserUuid: String) {
        viewModelScope.launch {
            try {
                _state.value = HomeUiState(loading = true)

                val created = repo.createIntake(CreateIntakeRequest(profileId, dispenserUuid))
                _state.value = _state.value.copy(intakeId = created.intakeId, status = created.status)

                // 2초 간격 폴링, 최대 10회
                repeat(10) {
                    delay(2000)
                    val s = repo.getIntake(created.intakeId)
                    _state.value = _state.value.copy(status = s.status)
                    if (s.status != "REQUESTED" && s.status != "PROCESSING") {
                        _state.value = _state.value.copy(loading = false)
                        return@launch
                    }
                }
                _state.value = _state.value.copy(loading = false)
            } catch (e: Exception) {
                _state.value = HomeUiState(error = e.message ?: "Unknown error")
            }
        }
    }
}