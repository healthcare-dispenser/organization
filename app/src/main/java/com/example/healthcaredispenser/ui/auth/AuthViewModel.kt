// AuthViewModel.kt
package com.example.healthcaredispenser.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// import com.example.healthcaredispenser.data.auth.TokenStore // 사용 안 함
import com.example.healthcaredispenser.data.model.auth.LoginRequest
import com.example.healthcaredispenser.data.model.auth.SignUpRequest
import com.example.healthcaredispenser.data.repository.AuthRepository
import com.example.healthcaredispenser.data.api.provideAuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.util.Log

data class AuthUiState(
    val loading: Boolean = false,
    val loggedIn: Boolean = false, // ⭐️ 로그인 상태는 여전히 관리
    val error: String? = null
)

// 로그인 성공 시 내비게이션을 위한 이벤트 정의
sealed class AuthNavigationEvent {
    data object NavigateToProfile : AuthNavigationEvent()
    // data object NavigateBackToWelcome : AuthNavigationEvent() // 필요 시 회원가입 성공 이벤트 추가 가능
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository(provideAuthApi())

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    // Navigation Event를 위한 SharedFlow 추가
    private val _navigationEvent = MutableSharedFlow<AuthNavigationEvent>()
    val navigationEvent: SharedFlow<AuthNavigationEvent> = _navigationEvent.asSharedFlow()

    fun signUp(email: String, password: String, passwordConfirm: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                // 1. 회원가입 API 호출 (성공 시 토큰이 반환되지만 사용 안 함)
                val res = repo.signUp(SignUpRequest(email, password, passwordConfirm))

                // 2. 상태 업데이트 (loggedIn은 false로 유지!)
                _state.value = _state.value.copy(loading = false, loggedIn = false, error = null) // 👈 loggedIn = false
                Log.d("AuthViewModel", "SignUp successful, state updated. loggedIn should be false.")

                // ⬇️ === 수정된 부분 === ⬇️
                // 3. 화면 이동 이벤트 발생 제거!
                // _navigationEvent.emit(AuthNavigationEvent.NavigateToProfile) // 👈 주석 처리 또는 삭제
                // Log.d("AuthViewModel", "Navigation event emitted after SignUp") // 👈 주석 처리 또는 삭제

                // ⭐️ (선택) 회원가입 성공 후 Welcome으로 돌아가라는 별도 이벤트 발생 가능
                // _navigationEvent.emit(AuthNavigationEvent.NavigateBackToWelcome)
                // ⬆️ =================== ⬆️

            } catch (e: Exception) {
                Log.e("AuthViewModel", "SignUp failed", e)
                _state.value = _state.value.copy(
                    loading = false,
                    error = when (e) {
                        is HttpException -> "회원가입 실패 (${e.code()})" // 메시지 구체화
                        is IOException -> "네트워크 오류"
                        else -> e.message ?: "알 수 없는 오류"
                    }
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val res = repo.login(LoginRequest(email, password))
                // ✅ 토큰 저장은 Repository에서 처리됨
                _state.value = _state.value.copy(loading = false, loggedIn = true)
                Log.d("AuthViewModel", "Login successful, state updated to loggedIn=true")

                // 로그인 성공 시 Navigation Event 발생 (변경 없음)
                _navigationEvent.emit(AuthNavigationEvent.NavigateToProfile)
                Log.d("AuthViewModel", "Navigation event emitted after Login")

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed", e)
                _state.value = _state.value.copy(
                    loading = false,
                    error = when (e) {
                        is HttpException -> "로그인 실패 (${e.code()})" // 메시지 구체화
                        is IOException -> "네트워크 오류"
                        else -> e.message ?: "알 수 없는 오류"
                    }
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _state.value = _state.value.copy(loggedIn = false)
            Log.d("AuthViewModel", "Logout successful, state updated to loggedIn=false")
        }
    }
}