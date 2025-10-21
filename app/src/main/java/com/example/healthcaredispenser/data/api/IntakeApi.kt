package com.example.healthcaredispenser.data.api

import com.example.healthcaredispenser.data.model.intake.CreateIntakeRequest
import com.example.healthcaredispenser.data.model.intake.CreateIntakeResponse
import com.example.healthcaredispenser.data.model.intake.IntakeStatusResponse
import com.example.healthcaredispenser.data.model.intake.ListIntakesRequest
import com.example.healthcaredispenser.data.model.intake.ListIntakesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path

interface IntakeApi {
    @POST("/api/intakes")
    suspend fun createIntake(@Body req: CreateIntakeRequest): CreateIntakeResponse

    @GET("/api/intakes/{intakeId}")
    suspend fun getIntake(@Path("intakeId") intakeId: Long): IntakeStatusResponse

    // Swagger가 body를 요구하므로 @HTTP 사용
    @HTTP(method = "GET", path = "/api/intakes", hasBody = true)
    suspend fun listIntakes(@Body req: ListIntakesRequest): ListIntakesResponse
}
