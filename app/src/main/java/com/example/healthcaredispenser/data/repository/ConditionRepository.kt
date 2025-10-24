package com.example.healthcaredispenser.data.repository

import com.example.healthcaredispenser.data.api.ConditionApi
import com.example.healthcaredispenser.data.api.provideConditionApi
import com.example.healthcaredispenser.data.model.condition.ConditionRecordResponseItem
import com.example.healthcaredispenser.data.model.condition.CreateConditionRecordRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate // ⭐️ 추가
import java.time.format.DateTimeFormatter // ⭐️ 추가

class ConditionRepository(
    private val api: ConditionApi = provideConditionApi()
) {
    suspend fun createConditionRecord(profileId: Long, req: CreateConditionRecordRequest): Result<Unit> = // 👈 intakeId -> profileId
        runCatching {
            val response = withContext(Dispatchers.IO) {
                api.createConditionRecord(profileId, req) // 👈 profileId 사용
            }
            if (!response.isSuccessful) {
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