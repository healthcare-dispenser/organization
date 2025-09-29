package com.example.healthcaredispenser.data.repository

import com.example.healthcaredispenser.data.api.ProfileApi
import com.example.healthcaredispenser.data.api.provideProfileApi
import com.example.healthcaredispenser.data.model.profile.CreateProfileRequest
import com.example.healthcaredispenser.data.model.profile.ProfileDto
import com.example.healthcaredispenser.data.model.profile.ProfileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Profile 데이터 관리
 * - API 통신 캡슐화
 * - suspend fun → runCatching 으로 Result 반환하면 ViewModel에서 안전하게 사용 가능
 */
class ProfileRepository(
    private val api: ProfileApi = provideProfileApi()
) {
    /** 생성: {id, name}만 반환되므로 ProfileItem */
    suspend fun createProfile(req: CreateProfileRequest): Result<ProfileItem> =
        runCatching {
            withContext(Dispatchers.IO) { api.create(req) }
        }

    /** 목록: 래퍼 → items 로 변환 */
    suspend fun fetchProfiles(): Result<List<ProfileDto>> =
        runCatching {
            withContext(Dispatchers.IO) { api.list().items }
        }

    /** 삭제: DELETE /api/profiles/{profileId} */
    suspend fun deleteProfile(id: Long): Result<Unit> =
        runCatching {
            val res = withContext(Dispatchers.IO) { api.delete(id) }
            if (res.isSuccessful) Unit else error("Delete failed: ${res.code()}")
        }
}
