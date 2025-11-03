package com.example.healthcaredispenser.data.repository

import com.example.healthcaredispenser.data.api.DispenserApi
import com.example.healthcaredispenser.data.api.RetrofitClient

object DispenserRepository {
    private val api = RetrofitClient.retrofit.create(DispenserApi::class.java)

    /** ✅ 세척 요청 */
    suspend fun wash(dispenserUuid: String, slot: Int) = api.wash(dispenserUuid, slot)
}
