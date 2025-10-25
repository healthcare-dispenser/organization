package com.example.healthcaredispenser.data.api

import com.example.healthcaredispenser.data.model.condition.ConditionHistoryResponse
import com.example.healthcaredispenser.data.model.condition.CreateConditionRecordRequest
import retrofit2.Response // ⭐️ Response 사용 (성공 여부만 확인)
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.GET

interface ConditionApi {
    @POST("api/profiles/{profileId}/feedbacks") // 👈 경로 변경
    suspend fun createConditionRecord(
        @Path("profileId") profileId: Long, // 👈 intakeId -> profileId
        @Body req: CreateConditionRecordRequest // 👈 요청 모델에 date 포함됨
    ): Response<Unit>

    @GET("api/profiles/{profileId}/feedbacks") // 👈 경로 변경 (/conditions-record -> /feedbacks)
    suspend fun getConditionHistory(
        @Path("profileId") profileId: Long
    ): ConditionHistoryResponse
}

// RetrofitClient에 ConditionApi를 생성하는 함수 추가 (선택 사항)
fun provideConditionApi(): ConditionApi =
    RetrofitClient.retrofit.create(ConditionApi::class.java)