package com.example.healthcaredispenser.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcaredispenser.data.model.intake.CreateIntakeRequest
import com.example.healthcaredispenser.data.repository.IntakeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.healthcaredispenser.data.api.RecommendationResponse // ✅ 모델 import
import android.util.Log // ✅ Log import

// ✅ UI 상태에 추천 배합 데이터(recommendation) 필드 추가
data class HomeUiState(
    val loading: Boolean = false, // 로딩 상태 (추천 배합 로딩 + 섭취 요청 폴링 시 true)
    val intakeId: Long? = null,
    val status: String? = null,
    val error: String? = null,
    val recommendation: RecommendationResponse? = null // 👈 추천 배합 데이터 (초기값 null)
)


class HomeViewModel(
    private val repo: IntakeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    // ✅ 화면 시작 시 추천 배합 로드하는 함수
    fun loadRecommendation(profileId: Long) {
        // 이미 로딩 중이면 중복 호출 방지 (선택 사항)
        if (_state.value.loading && _state.value.recommendation == null) return

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null) // 로딩 시작
            repo.getRecommendation(profileId)
                .onSuccess { rec ->
                    Log.d("HomeViewModel", "Recommendation loaded: $rec")
                    _state.value = _state.value.copy(
                        loading = false, // 로딩 종료
                        recommendation = rec // 👈 상태 업데이트
                    )
                }
                .onFailure { e ->
                    Log.e("HomeViewModel", "Failed to load recommendation", e)
                    _state.value = _state.value.copy(
                        loading = false, // 로딩 종료
                        error = e.message ?: "추천 배합 정보를 불러오지 못했습니다." // 에러 메시지 설정
                    )
                }
        }
    }

    // 섭취 요청 함수 수정 (로딩 상태 처리 변경)
    fun requestIntake(profileId: Long, dispenserUuid: String) {
        viewModelScope.launch {
            // 중복 요청 방지 (변경 없음)
            if (_state.value.loading && _state.value.status != null) return@launch

            try {
                // 로딩 시작 (변경 없음)
                _state.value = _state.value.copy(loading = true, error = null, status = null, intakeId = null)

                // 섭취 요청 생성 (변경 없음)
                val created = repo.createIntake(CreateIntakeRequest(profileId, dispenserUuid))
                _state.value = _state.value.copy(intakeId = created.intakeId, status = created.status)

                // ✅ for 루프로 변경 + break 사용
                var pollingSuccessful = false // 성공/실패 여부 추적
                for (i in 1..10) { // 최대 10번 반복
                    delay(2000) // 2초 대기
                    val s = repo.getIntake(created.intakeId) // 상태 조회
                    _state.value = _state.value.copy(status = s.status) // 상태 업데이트

                    // 상태가 확정되면 (REQUESTED나 PROCESSING이 아니면)
                    if (s.status != "REQUESTED" && s.status != "PROCESSING") {
                        _state.value = _state.value.copy(loading = false) // 로딩 종료
                        pollingSuccessful = true // 성공/실패 플래그 설정
                        break // ✅ for 루프 탈출! (오류 없음)
                    }
                }

                // ✅ 폴링 타임아웃 처리: 10번 다 돌았는데 성공/실패 안 했으면 로딩 종료
                if (!pollingSuccessful) {
                    Log.w("HomeViewModel", "Polling timed out for intake ${created.intakeId}")
                    _state.value = _state.value.copy(loading = false)
                }

            } catch (e: Exception) {
                // 에러 처리 (변경 없음)
                Log.e("HomeViewModel", "Intake request failed", e)
                _state.value = _state.value.copy(loading = false, error = e.message ?: "섭취 요청 중 오류가 발생했습니다.")
            }
            // 코루틴 launch 블록은 여기서 자연스럽게 종료됨
        }
    }

    // ✅ (선택) UI에서 에러 메시지를 보여준 후 호출하여 초기화하는 함수
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}