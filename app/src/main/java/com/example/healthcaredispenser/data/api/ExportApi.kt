package com.example.healthcaredispenser.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * 데이터 내보내기(Export) 관련 API
 */
interface ExportApi {

    /**
     * 섭취 기록 및 피드백 데이터를 CSV 파일로 다운로드합니다.
     * @Streaming : 응답 본문이 클 수 있으므로 스트리밍으로 받습니다.
     * @return ResponseBody : 원시 파일 데이터를 받기 위해 ResponseBody를 사용합니다.
     */
    @Streaming // 파일 다운로드를 위해 스트리밍 어노테이션 사용
    @Headers("Accept: text/csv")
    @GET("api/exports/intake-feedback.csv") // 백엔드에서 전달받은 경로
    suspend fun exportIntakeFeedback(
        @Query("profileId") profileId: Long // 👈 profileId 추가!
    ): ResponseBody
}

/**
 * ExportApi 인스턴스를 제공하는 함수
 */
fun provideExportApi(): ExportApi =
    RetrofitClient.retrofit.create(ExportApi::class.java)