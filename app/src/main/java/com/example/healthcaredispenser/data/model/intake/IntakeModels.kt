package com.example.healthcaredispenser.data.model.intake

// POST /api/intakes
data class CreateIntakeRequest(
    val profileId: Long,
    val dispenserUuid: String
)
data class CreateIntakeResponse(
    val intakeId: Long,
    val status: String // "REQUESTED"
)

// GET /api/intakes/{intakeId}
data class IntakeStatusResponse(
    val intakeId: Long,
    val status: String // "REQUESTED" | "PROCESSING" | "SUCCESS" | "FAIL" (서버 정의에 따름)
)

// GET /api/intakes (목록)
data class ListIntakesRequest(
    val profileId: Long,
    val dispenserUuid: String
)
data class ListIntakesResponse(
    val items: List<IntakeItem>,
    val count: Int
)

data class IntakeItem(
    val intakeId: Long,
    val vitamin: Double? = null,
    val melatonin: Double? = null,
    val magnesium: Double? = null,
    val electrolyte: Double? = null,
    val status: String,
    val profileSnapshot: String? = null,
    val requestedAt: String? = null,
    val completedAt: String? = null
)
