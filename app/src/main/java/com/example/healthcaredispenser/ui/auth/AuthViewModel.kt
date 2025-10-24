// AuthViewModel.kt
package com.example.healthcaredispenser.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcaredispenser.data.auth.TokenStore // 사용하지 않지만 일단 둠
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
    val loggedIn: Boolean = false,
    val error: String? = null
)

// 로그인 성공 시 내비게이션을 위한 이벤트 정의
sealed class AuthNavigationEvent {
    data object NavigateToProfile : AuthNavigationEvent()
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
                val res = repo.signUp(SignUpRequest(email, password, passwordConfirm))
                // ✅ 토큰 저장 Repository에서 처리
                _state.value = _state.value.copy(loading = false, loggedIn = true)
                Log.d("AuthViewModel", "SignUp successful, state updated to loggedIn=true") // signUp 로그로 수정
                // 로그인 성공(여기서는 회원가입 성공) 시 Navigation Event 발생시키기
                _navigationEvent.emit(AuthNavigationEvent.NavigateToProfile)
                Log.d("AuthViewModel", "Navigation event emitted after SignUp") // signUp 로그로 수정
            } catch (e: Exception) {
                Log.e("AuthViewModel", "SignUp failed", e) // signUp 로그로 수정
                _state.value = _state.value.copy(
                    loading = false,
                    error = when (e) {
                        is HttpException -> "서버 오류(${e.code()})"
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
                // ✅ 토큰 저장 Repository에서 처리
                _state.value = _state.value.copy(loading = false, loggedIn = true)
                Log.d("AuthViewModel", "Login successful, state updated to loggedIn=true")

                // ⬇️ === 추가된 부분 === ⬇️
                // 로그인 성공 시 Navigation Event 발생시키기
                _navigationEvent.emit(AuthNavigationEvent.NavigateToProfile)
                Log.d("AuthViewModel", "Navigation event emitted after Login") // Login 로그로 수정
                // ⬆️ =================== ⬆️

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed", e)
                _state.value = _state.value.copy(
                    loading = false,
                    error = when (e) {
                        is HttpException -> "서버 오류(${e.code()})"
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
            Log.d("AuthViewModel", "Logout successful, state updated to loggedIn=false") // 로그 추가
        }
    }
}