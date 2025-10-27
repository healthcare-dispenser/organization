package com.example.healthcaredispenser.data.repository

import com.example.healthcaredispenser.data.api.IntakeApi
import com.example.healthcaredispenser.data.api.RecommendationResponse
import com.example.healthcaredispenser.data.model.intake.CreateIntakeRequest
import com.example.healthcaredispenser.data.model.intake.ListIntakesRequest
import com.example.healthcaredispenser.data.model.intake.ListIntakesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IntakeRepository(private val api: IntakeApi) {

    suspend fun createIntake(req: CreateIntakeRequest) = api.createIntake(req)

    suspend fun getIntake(intakeId: Long) = api.getIntake(intakeId)

    /** 신 엔드포인트: 페이지/사이즈 명시 (기본 size=200) */
    suspend fun listIntakesByProfile(
        profileId: Long,
        page: Int? = null,
        size: Int = 200
    ) = api.listIntakesByProfile(
        profileId = profileId,
        page = page,
        size = size,
        from = null,
        to = null,
        status = null
    )

    /** 구 엔드포인트(바디 GET) – 호환용 */
    suspend fun listIntakesLegacy(req: ListIntakesRequest) =
        api.listIntakes(req)

    /**
     * 상황에 따라 자동 폴백 (신 → 구).
     * 신 엔드포인트는 size=200으로 넉넉히 요청해 서버 기본값(예: 7) 제한을 회피.
     */
    suspend fun listIntakesSmart(
        profileId: Long,
        dispenserUuid: String = "",
        size: Int = 200
    ): ListIntakesResponse = try {
        listIntakesByProfile(profileId = profileId, size = size)
    } catch (_: Exception) {
        api.listIntakes(ListIntakesRequest(profileId = profileId, dispenserUuid = dispenserUuid))
    }

    /** 홈 추천 배합 조회 (안전하게 Result로 래핑) */
    suspend fun getRecommendation(profileId: Long): Result<RecommendationResponse> =
        runCatching {
            withContext(Dispatchers.IO) {
                api.getRecommendation(profileId)
            }
        }
}
