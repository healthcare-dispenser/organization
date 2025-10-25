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
import retrofit2.http.Query

interface IntakeApi {
    @POST("/api/intakes")
    suspend fun createIntake(@Body req: CreateIntakeRequest): CreateIntakeResponse

    @GET("/api/intakes/{intakeId}")
    suspend fun getIntake(@Path("intakeId") intakeId: Long): IntakeStatusResponse

    // ✅ 신 엔드포인트: GET /api/profiles/{profileId}/intakes (Swagger 캡쳐 기준)
    @GET("/api/profiles/{profileId}/intakes")
    suspend fun listIntakesByProfile(
        @Path("profileId") profileId: Long,
        // 서버에서 지원하면 페이지/필터 쿼리에도 확장 가능
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("status") status: String? = null
    ): ListIntakesResponse

    // ⬇️ 구 엔드포인트(바디가 필요한 GET). 기존 코드와 호환 위해 유지
    @HTTP(method = "GET", path = "/api/intakes", hasBody = true)
    suspend fun listIntakes(@Body req: ListIntakesRequest): ListIntakesResponse
}
