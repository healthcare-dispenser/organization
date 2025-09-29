package com.example.healthcaredispenser.data.repository

import com.example.healthcaredispenser.data.api.IntakeApi
import com.example.healthcaredispenser.data.model.intake.CreateIntakeRequest
import com.example.healthcaredispenser.data.model.intake.ListIntakesRequest

class IntakeRepository(private val api: IntakeApi) {
    suspend fun createIntake(req: CreateIntakeRequest) = api.createIntake(req)
    suspend fun getIntake(intakeId: Long) = api.getIntake(intakeId)
    suspend fun listIntakes(req: ListIntakesRequest) = api.listIntakes(req)
}