package com.example.healthcaredispenser.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcaredispenser.data.model.profile.CreateProfileRequest
import com.example.healthcaredispenser.data.model.profile.ProfileDto
import com.example.healthcaredispenser.data.model.profile.ProfileItem
import com.example.healthcaredispenser.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class ProfileUiState(
    val loading: Boolean = false,              // 목록 로딩
    val profiles: List<ProfileDto> = emptyList(),
    val saving: Boolean = false,               // 생성 중
    val saved: Boolean = false,                // 생성 성공 플래그
    val created: ProfileItem? = null,          // 생성 직후 반환값 {id, name}
    val unauthorized: Boolean = false,         // 401 처리용
    val error: String? = null
)

class ProfileViewModel(
    private val repo: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui.asStateFlow()

    /** 목록 불러오기 */
    fun fetch() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null, unauthorized = false)

            repo.fetchProfiles()
                .onSuccess { list ->
                    _ui.value = _ui.value.copy(
                        loading = false,
                        profiles = list,
                        error = null,
                        unauthorized = false
                    )
                }
                .onFailure { e ->
                    Log.e("ProfileViewModel", "fetch error", e)
                    _ui.value = _ui.value.copy(
                        loading = false,
                        error = humanReadable(e),
                        unauthorized = isUnauthorized(e)
                    )
                }
        }
    }

    /** 다시 시도 버튼용 */
    fun retryFetch() = fetch()

    /**
     * 프로필 생성
     * - 성공 시 saved=true, created=ProfileItem 보관
     * - 즉시 목록에 placeholder 추가 후, 서버 최신 목록으로 동기화(fetch)
     */
    fun create(
        req: CreateProfileRequest,
        afterSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(
                saving = true,
                error = null,
                saved = false,
                unauthorized = false
            )

            repo.createProfile(req)
                .onSuccess { item ->
                    // 1) 즉시 반영 (placeholder)
                    val appended = _ui.value.profiles + ProfileDto(
                        id = item.id,
                        name = item.name
                        // 나머지 필드는 GET 동기화 시 채워짐
                    )

                    _ui.value = _ui.value.copy(
                        saving = false,
                        saved = true,
                        created = item,
                        error = null,
                        unauthorized = false,
                        profiles = appended
                    )

                    // 2) 서버 최신값으로 동기화
                    fetch()

                    // 3) 후처리 콜백(네비게이션 등)
                    afterSuccess?.invoke()
                }
                .onFailure { e ->
                    Log.e("ProfileViewModel", "create error", e)
                    _ui.value = _ui.value.copy(
                        saving = false,
                        saved = false,
                        created = null,
                        error = humanReadable(e),
                        unauthorized = isUnauthorized(e)
                    )
                }
        }
    }

    /** 프로필 삭제 (옵티미스틱 → 실패 시 롤백) */
    fun delete(id: Long) {
        viewModelScope.launch {
            val before = _ui.value.profiles

            // 1) 먼저 UI에서 제거
            _ui.value = _ui.value.copy(
                profiles = before.filterNot { it.id == id },
                error = null
            )

            // 2) 서버 호출
            repo.deleteProfile(id)
                .onSuccess {
                    // 선택: 서버 상태 재동기화
                    fetch()
                }
                .onFailure { e ->
                    Log.e("ProfileViewModel", "delete error", e)
                    // 실패 → 롤백 + 에러 표시
                    _ui.value = _ui.value.copy(
                        profiles = before,
                        error = humanReadable(e),
                        unauthorized = isUnauthorized(e)
                    )
                }
        }
    }

    /** 저장 성공 플래그 및 created 소비 */
    fun clearSavedFlag() {
        _ui.value = _ui.value.copy(saved = false, created = null)
    }

    /** 에러 메시지 변환 */
    private fun humanReadable(e: Throwable): String = when (e) {
        is HttpException -> {
            when (e.code()) {
                401 -> "로그인이 필요합니다. 다시 로그인해 주세요."
                403 -> "접근 권한이 없습니다."
                404 -> "리소스를 찾을 수 없습니다."
                415 -> "지원하지 않는 요청 형식입니다."
                422 -> "요청 값이 올바르지 않습니다."
                500 -> "서버 오류가 발생했습니다."
                else -> "요청 실패 (HTTP ${e.code()})"
            }
        }
        is IOException -> "네트워크 연결을 확인해 주세요."
        else -> e.message ?: "알 수 없는 오류가 발생했습니다."
    }

    private fun isUnauthorized(e: Throwable): Boolean =
        (e as? HttpException)?.code() == 401
}
