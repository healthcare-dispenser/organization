package com.example.healthcaredispenser.data.model.condition // 👈 패키지 경로 선언 확인!

import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 요청 모델 (수정 없음)
data class CreateConditionRecordRequest(
    val sleepQuality: Int,
    val fatigueLevel: Int
)

// GET 응답의 아이템 모델 (feedbackId 추가)
data class ConditionRecordResponseItem(
    val feedbackId: Long, // ⭐️ 추가 (Long 타입으로 가정)
    val recordDate: String, // 시간 포함된 ISO 형식 문자열 (예: "2025-10-25T06:54:50.977Z")
    val sleepQuality: Int,
    val fatigueLevel: Int
)

// GET 응답 전체를 감싸는 모델 (Swagger 형식)
data class ConditionHistoryResponse(
    val items: List<ConditionRecordResponseItem>,
    val count: Int
)