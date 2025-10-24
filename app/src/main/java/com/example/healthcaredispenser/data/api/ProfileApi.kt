package com.example.healthcaredispenser.data.api

import com.example.healthcaredispenser.data.model.profile.CreateProfileRequest
import com.example.healthcaredispenser.data.model.profile.ProfileItem
import com.example.healthcaredispenser.data.model.profile.ProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH // ⭐️ PUT -> PATCH 로 변경
import retrofit2.http.POST
// import retrofit2.http.PUT // ⭐️ 제거
import retrofit2.http.Path

interface ProfileApi {
    /** 생성: {id, name} */
    @POST("api/profiles")
    suspend fun create(
        @Body req: CreateProfileRequest
    ): ProfileItem

    /** 목록: 래퍼 구조 */
    @GET("api/profiles")
    suspend fun list(): ProfileResponse

    /** ⭐️ 수정: PATCH /api/profiles/{profileId} */
    // ⬇️ === 수정된 부분 === ⬇️
    @PATCH("api/profiles/{profileId}") // @PUT -> @PATCH
    suspend fun update(
        @Path("profileId") id: Long,
        @Body req: CreateProfileRequest
    ): ProfileItem // ⭐️ 수정 성공 시 {id, name} 반환 (create와 동일 가정)
    // ⬆️ =================== ⬆️


    /** 삭제: DELETE /api/profiles/{profileId} */
    @DELETE("api/profiles/{profileId}")
    suspend fun delete(
        @Path("profileId") id: Long
    ): Response<Unit>
}

fun provideProfileApi(): ProfileApi =
    RetrofitClient.retrofit.create(ProfileApi::class.java)