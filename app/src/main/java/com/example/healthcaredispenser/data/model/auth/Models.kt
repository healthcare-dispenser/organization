package com.example.healthcaredispenser.data.model.auth

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

// data/model/auth/AuthResponse.kt
data class AuthResponse(
    @SerializedName("token") val token: String
    // accessToken 으로 통일하고 싶으면: @SerializedName("token") val accessToken: String
)

