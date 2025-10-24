package com.example.healthcaredispenser.data.api

import com.example.healthcaredispenser.data.model.condition.ConditionRecordResponseItem
import com.example.healthcaredispenser.data.model.condition.CreateConditionRecordRequest
import retrofit2.Response // ⭐️ Response 사용 (성공 여부만 확인)
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.GET

interface ConditionApi {
    @POST("api/profiles/{profileId}/conditions-record")
    suspend fun createConditionRecord(
        @Path("profileId") profileId: Long,
        @Body req: CreateConditionRecordRequest
    ): Response<Unit> // ⭐️ 성공 여부만 받음 (본문 없음)

    @GET("api/profiles/{profileId}/conditions-record")
    suspend fun getConditionHistory(
        @Path("profileId") profileId: Long
    ): List<ConditionRecordResponseItem> // ⭐️ 응답: 기록 목록 List
}

// RetrofitClient에 ConditionApi를 생성하는 함수 추가 (선택 사항)
fun provideConditionApi(): ConditionApi =
    RetrofitClient.retrofit.create(ConditionApi::class.java)