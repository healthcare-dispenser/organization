// AuthViewModel.kt
package com.example.healthcaredispenser.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcaredispenser.data.auth.TokenStore
import com.example.healthcaredispenser.data.model.auth.LoginRequest
import com.example.healthcaredispenser.data.model.auth.SignUpRequest
import com.example.healthcaredispenser.data.repository.AuthRepository
import com.example.healthcaredispenser.data.api.provideAuthApi // 너의 프로젝트에 맞춰 import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class AuthUiState(
    val loading: Boolean = false,
    val loggedIn: Boolean = false,
    val error: String? = null
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository(provideAuthApi())

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun signUp(email: String, password: String, passwordConfirm: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val res = repo.signUp(SignUpRequest(email, password, passwordConfirm))
                // ✅ 토큰 저장 (SharedPreferences)
                //TokenStore.set(res.token)
                _state.value = _state.value.copy(loading = false, loggedIn = true)
            } catch (e: Exception) {
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
                // ✅ 토큰 저장 (SharedPreferences)
                //TokenStore.set(res.token)
                _state.value = _state.value.copy(loading = false, loggedIn = true)
            } catch (e: Exception) {
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
        }
    }
}
