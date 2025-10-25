package com.example.healthcaredispenser.data.repository

import com.example.healthcaredispenser.data.api.ConditionApi
import com.example.healthcaredispenser.data.api.provideConditionApi
import com.example.healthcaredispenser.data.model.condition.ConditionHistoryResponse
import com.example.healthcaredispenser.data.model.condition.ConditionRecordResponseItem
import com.example.healthcaredispenser.data.model.condition.CreateConditionRecordRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ConditionRepository(
    private val api: ConditionApi = provideConditionApi()
) {
    suspend fun createConditionRecord(
        profileId: Long,
        req: CreateConditionRecordRequest
    ): Result<Unit> = // 👈 intakeId -> profileId
        runCatching {
            val response = withContext(Dispatchers.IO) {
                api.createConditionRecord(profileId, req) // 👈 profileId 사용
            }
            if (!response.isSuccessful) {
                error("Record creation failed: ${response.code()} - ${response.message()}")
            }
        }

    // 목록 조회 (응답 처리 방식 변경)
    suspend fun getConditionHistory(profileId: Long): Result<List<ConditionRecordResponseItem>> =
        runCatching {
            withContext(Dispatchers.IO) {
                // ⭐️ api.getConditionHistory 호출 결과는 ConditionHistoryResponse 객체
                // ⭐️ 그 안의 items 리스트를 반환하도록 수정
                api.getConditionHistory(profileId).items
            }
        }
}