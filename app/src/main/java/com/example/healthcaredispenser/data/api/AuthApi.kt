package com.example.healthcaredispenser.data.api

import com.example.healthcaredispenser.data.model.auth.AuthResponse
import com.example.healthcaredispenser.data.model.auth.LoginRequest
import com.example.healthcaredispenser.data.model.auth.SignUpRequest
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @Headers("Accept: application/json")
    @POST("api/accounts/signup")
    suspend fun signUp(@Body body: SignUpRequest): AuthResponse

    @Headers("Accept: application/json")
    @POST("api/accounts/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse
}

fun provideAuthApi(): AuthApi =
    RetrofitClient.retrofit.create(AuthApi::class.java)
