package com.example.healthcaredispenser.data.repository

import com.example.healthcaredispenser.data.api.IntakeApi
import com.example.healthcaredispenser.data.model.intake.CreateIntakeRequest
import com.example.healthcaredispenser.data.model.intake.ListIntakesRequest

class IntakeRepository(private val api: IntakeApi) {

    suspend fun createIntake(req: CreateIntakeRequest) = api.createIntake(req)
    suspend fun getIntake(intakeId: Long) = api.getIntake(intakeId)

    // ✅ 신 엔드포인트(프로필 경로) – 1순위
    suspend fun listIntakesByProfile(profileId: Long) =
        api.listIntakesByProfile(profileId = profileId)

    // ⬇️ 구 엔드포인트(바디 GET) – 호환용
    suspend fun listIntakesLegacy(req: ListIntakesRequest) =
        api.listIntakes(req)

    // ✅ 상황에 따라 자동 폴백 (신 → 구)
    suspend fun listIntakesSmart(profileId: Long, dispenserUuid: String = "") =
        try {
            api.listIntakesByProfile(profileId)
        } catch (_: Exception) {
            api.listIntakes(ListIntakesRequest(profileId = profileId, dispenserUuid = dispenserUuid))
        }
}
