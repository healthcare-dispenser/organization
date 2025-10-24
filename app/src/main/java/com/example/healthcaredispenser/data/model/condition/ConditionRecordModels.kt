package com.example.healthcaredispenser.data.model.condition // 👈 패키지 경로 선언 확인!

// 요청 모델 (수정 없음)
data class CreateConditionRecordRequest(
    val sleepQuality: Int,
    val fatigueLevel: Int
)

// ⭐️ 응답 목록 아이템 모델 (이 부분이 정확해야 합니다)
data class ConditionRecordResponseItem(
    val recordDate: String, // 서버에서 문자열로 준다고 가정 (예: "2025-10-24")
    val sleepQuality: Int,
    val fatigueLevel: Int
)