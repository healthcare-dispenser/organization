package com.example.healthcaredispenser.data.api

import com.example.healthcaredispenser.data.model.profile.CreateProfileRequest
import com.example.healthcaredispenser.data.model.profile.ProfileItem
import com.example.healthcaredispenser.data.model.profile.ProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
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

    /** 삭제: DELETE /api/profiles/{profileId} */
    @DELETE("api/profiles/{profileId}")
    suspend fun delete(
        @Path("profileId") id: Long
    ): Response<Unit>
}

fun provideProfileApi(): ProfileApi =
    RetrofitClient.retrofit.create(ProfileApi::class.java)
