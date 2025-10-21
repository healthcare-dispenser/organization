package com.example.healthcaredispenser.data.api

import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterDispenserRequest(val dispenserUuid: String)
data class RegisterDispenserResponse(val dispenserUuid: String)

interface DispenserApi {
    @POST("api/dispensers")
    suspend fun register(@Body req: RegisterDispenserRequest): RegisterDispenserResponse
}
