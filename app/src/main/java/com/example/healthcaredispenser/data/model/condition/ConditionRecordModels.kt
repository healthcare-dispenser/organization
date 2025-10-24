package com.example.healthcaredispenser.data.model.condition // 👈 패키지 경로 선언 확인!

import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 요청 모델 (수정 없음)
data class CreateConditionRecordRequest(
    val recordDate: String, // ⭐️ 날짜 필드 추가 (YYYY-MM-DD 형식 문자열)
    val sleepQuality: Int,
    val fatigueLevel: Int
)

// ⭐️ 응답 목록 아이템 모델
data class ConditionRecordResponseItem(
    val recordDate: String, // 서버에서 문자열로 준다고 가정 (예: "2025-10-24")
    val sleepQuality: Int,
    val fatigueLevel: Int
)