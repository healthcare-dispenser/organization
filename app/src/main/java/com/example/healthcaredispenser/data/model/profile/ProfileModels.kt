package com.example.healthcaredispenser.data.model.profile

/**
 * POST /api/profiles 요청 바디
 * - Swagger 예시 기준
 */
data class CreateProfileRequest(
    val name: String,
    val height: Double,
    val weight: Double,
    val gender: String,          // "MALE" | "FEMALE"
    val tags: List<String>,      // 예: ["ALCOHOL"]
    val conditions: List<String> // 예: ["PREGNANT"]
)

/**
 * POST /api/profiles 응답 전용
 * - Swagger 예시: { "id": 0, "name": "string" }
 */
data class ProfileItem(
    val id: Long,
    val name: String
)

/**
 * 프로필 DTO
 * - GET /api/profiles 등 상세 조회/목록 응답에 사용
 * - height/weight/gender/tags/conditions 포함 가능
 */
data class ProfileDto(
    val id: Long? = null,
    val name: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val gender: String? = null,
    val tags: List<String>? = null,
    val conditions: List<String>? = null
)

/**
 * ✅ GET /api/profiles 응답 래퍼
 */
data class ProfileResponse(
    val items: List<ProfileDto> = emptyList(),
    val count: Int = 0
)
