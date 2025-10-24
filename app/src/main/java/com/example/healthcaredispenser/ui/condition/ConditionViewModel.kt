package com.example.healthcaredispenser.ui.condition // 폴더 경로 확인

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcaredispenser.data.model.condition.ConditionRecordResponseItem
import com.example.healthcaredispenser.data.model.condition.CreateConditionRecordRequest
import com.example.healthcaredispenser.data.repository.ConditionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate // ⭐️ 추가 (날짜 변환용)
import java.time.format.DateTimeParseException // ⭐️ 추가 (날짜 변환용)

// UI 상태 정의 (로딩, 에러, 성공 이벤트)
data class ConditionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val conditionHistory: List<ConditionRecordResponseItem> = emptyList() // ⭐️ 기록 목록 추가
)

// 화면 이동 등 일회성 이벤트를 위한 정의
sealed class ConditionNavigationEvent {
    data object NavigateBack : ConditionNavigationEvent()
}

class ConditionViewModel(
    private val repository: ConditionRepository = ConditionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConditionUiState())
    val uiState: StateFlow<ConditionUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<ConditionNavigationEvent>()
    val navigationEvent: SharedFlow<ConditionNavigationEvent> = _navigationEvent.asSharedFlow()

    fun saveConditionRecord(profileId: Long, sleepQuality: Int, fatigueLevel: Int) {
        viewModelScope.launch {
            _uiState.value = ConditionUiState(isLoading = true) // 로딩 시작

            val request = CreateConditionRecordRequest(
                sleepQuality = sleepQuality,
                fatigueLevel = fatigueLevel
            )

            repository.createConditionRecord(profileId, request)
                .onSuccess {
                    Log.d("ConditionViewModel", "Record saved successfully for profile $profileId")
                    _uiState.value = ConditionUiState(isLoading = false) // 로딩 종료
                    _navigationEvent.emit(ConditionNavigationEvent.NavigateBack) // 성공 시 뒤로가기 이벤트
                }
                .onFailure { e ->
                    Log.e("ConditionViewModel", "Failed to save record for profile $profileId", e)
                    _uiState.value = ConditionUiState(
                        isLoading = false,
                        error = humanReadableError(e) // 사용자 친화적 에러 메시지
                    )
                }
        }
    }

    // ⬇️ === 추가된 부분 (기록 목록 불러오기) === ⬇️
    fun fetchConditionHistory(profileId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true) // 로딩 시작
            repository.getConditionHistory(profileId)
                .onSuccess { historyList ->
                    Log.d("ConditionViewModel", "Fetched ${historyList.size} condition records for profile $profileId")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        conditionHistory = historyList // ⭐️ 상태 업데이트
                    )
                }
                .onFailure { e ->
                    Log.e("ConditionViewModel", "Failed to fetch condition history for profile $profileId", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = humanReadableError(e)
                    )
                }
        }
    }
    // 에러 메시지 처리 (ProfileViewModel과 유사)
    private fun humanReadableError(e: Throwable): String = when (e) {
        is HttpException -> "서버 오류 (${e.code()})"
        is IOException -> "네트워크 연결을 확인해 주세요."
        else -> e.message ?: "알 수 없는 오류가 발생했습니다."
    }

    // UI에서 에러 메시지를 소비한 후 호출
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}