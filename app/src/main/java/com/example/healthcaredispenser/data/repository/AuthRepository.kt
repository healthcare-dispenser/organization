package com.example.healthcaredispenser.data.repository

import com.example.healthcaredispenser.data.api.AuthApi
import com.example.healthcaredispenser.data.api.provideAuthApi
import com.example.healthcaredispenser.data.auth.TokenStore
import com.example.healthcaredispenser.data.model.auth.AuthResponse
import com.example.healthcaredispenser.data.model.auth.LoginRequest
import com.example.healthcaredispenser.data.model.auth.SignUpRequest

class AuthRepository(
    private val api: AuthApi = provideAuthApi()
) {
    suspend fun signUp(req: SignUpRequest): AuthResponse {
        val res = api.signUp(req)
        // 서버 응답 필드는 token 이지만, 모델에서 accessToken 으로 통일되어 있음.
        TokenStore.set(res.token)
        return res
    }

    suspend fun login(req: LoginRequest): AuthResponse {
        val res = api.login(req)
        TokenStore.set(res.token)
        return res
    }

    suspend fun logout() {
        TokenStore.clear()
    }
}
