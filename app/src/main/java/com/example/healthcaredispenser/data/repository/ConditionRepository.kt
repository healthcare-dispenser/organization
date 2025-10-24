package com.example.healthcaredispenser.data.repository

import com.example.healthcaredispenser.data.api.ConditionApi
import com.example.healthcaredispenser.data.api.provideConditionApi
import com.example.healthcaredispenser.data.model.condition.ConditionRecordResponseItem
import com.example.healthcaredispenser.data.model.condition.CreateConditionRecordRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConditionRepository(
    private val api: ConditionApi = provideConditionApi()
) {
    suspend fun createConditionRecord(profileId: Long, req: CreateConditionRecordRequest): Result<Unit> =
        runCatching {
            val response = withContext(Dispatchers.IO) {
                api.createConditionRecord(profileId, req)
            }
            if (response.isSuccessful) {
                Unit // 성공 시 Unit 반환
            } else {
                // HTTP 오류 코드와 메시지로 에러 생성
                error("Record creation failed: ${response.code()} - ${response.message()}")
            }
        }

    suspend fun getConditionHistory(profileId: Long): Result<List<ConditionRecordResponseItem>> =
        runCatching {
            withContext(Dispatchers.IO) {
                api.getConditionHistory(profileId)
            }
        }
}