package com.example.healthcaredispenser.data.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 요청 바디는 스웨거 스펙에 맞춰 { "uuid": "..." } 형태로 보냅니다.
 */
data class RegisterDispenserRequest(
    val uuid: String
)

/**
 * 스웨거 예시 응답은 { "accountId": 0, "uuid": "string" } 이므로
 * accountId는 널 허용(서버에서 안 보내는 경우 대비)로 둡니다.
 */
data class RegisterDispenserResponse(
    val accountId: Long? = null,
    val uuid: String
)

interface DispenserApi {

    /**
     * 디스펜서 등록
     * POST /api/dispensers
     * Body: { "uuid": "<스캔결과(또는 정규화된) uuid>" }
     */
    @POST("api/dispensers")
    suspend fun register(
        @Body req: RegisterDispenserRequest
    ): RegisterDispenserResponse

    /**
     * 세척 요청
     * POST /api/dispensers/{dispenserUuid}/wash/{slot}
     * Body 없음
     */
    @POST("api/dispensers/{dispenserUuid}/wash/{slot}")
    suspend fun wash(
        @Path("dispenserUuid") dispenserUuid: String,
        @Path("slot") slot: Int
    )
}
