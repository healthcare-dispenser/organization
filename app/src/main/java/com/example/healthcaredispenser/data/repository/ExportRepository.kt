package com.example.healthcaredispenser.data.repository

import com.example.healthcaredispenser.data.api.ExportApi
import com.example.healthcaredispenser.data.api.provideExportApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class ExportRepository(
    private val api: ExportApi = provideExportApi()
) {

    /**
     * CSV 데이터를 서버로부터 다운로드합니다.
     */
    suspend fun exportIntakeFeedback(): Result<ResponseBody> = runCatching {
        withContext(Dispatchers.IO) {
            api.exportIntakeFeedback()
        }
    }
}