package com.example.healthcaredispenser.data.network

import com.example.healthcaredispenser.data.auth.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath // ex) /api/accounts/login

        val builder = original.newBuilder()
            .header("Accept", "application/json")

        builder.removeHeader("Authorization")

        // 로그인/회원가입 요청은 Authorization 헤더 붙이지 않음
        val isAuthEndpoint =
            path.startsWith("/api/accounts/login") ||
                    path.startsWith("/api/accounts/signup") ||
                    path.startsWith("/api/accounts/sign-up")

        if (!isAuthEndpoint) {
            val token = TokenStore.get()
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        // JSON 바디 있는 요청이면 Content-Type 보장
        if (original.body != null) {
            builder.header("Content-Type", "application/json; charset=UTF-8")
        }

        return chain.proceed(builder.build())
    }
}
