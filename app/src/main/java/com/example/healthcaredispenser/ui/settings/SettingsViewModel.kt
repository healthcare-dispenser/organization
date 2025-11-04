package com.example.healthcaredispenser.ui.settings

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcaredispenser.data.repository.ExportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ✅ UI 상태 데이터
data class SettingsUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class SettingsViewModel(
    private val exportRepo: ExportRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    /**
     * CSV 파일 내보내기
     */
    fun exportData(context: Context) {
        if (_state.value.loading) return
        viewModelScope.launch {
            _state.value = SettingsUiState(loading = true)
            exportRepo.exportIntakeFeedback()
                .onSuccess { responseBody ->
                    try {
                        saveCsvFile(context, responseBody)
                        _state.value = SettingsUiState(successMessage = "데이터를 '다운로드' 폴더에 저장했습니다.")
                    } catch (e: IOException) {
                        Log.e("SettingsViewModel", "File save failed", e)
                        _state.value = SettingsUiState(error = "파일 저장에 실패했습니다: ${e.message}")
                    }
                }
                .onFailure { e ->
                    Log.e("SettingsViewModel", "Export failed", e)
                    _state.value = SettingsUiState(error = e.message ?: "데이터 내보내기에 실패했습니다.")
                }
        }
    }

    /**
     * ✅ 파일 저장 (스마트캐스트 오류 및 리소스 누수 완전 제거 버전)
     */
    private fun saveCsvFile(context: Context, body: ResponseBody) {
        val resolver = context.contentResolver
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "intake-feedback-$timeStamp.csv"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
        }

        var uri: android.net.Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // ✅ MediaStore (Android 10 이상)
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)

                uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IOException("Failed to create MediaStore entry (API 29+)")

                resolver.openOutputStream(uri)?.use { outputStream ->
                    body.byteStream().use { input ->
                        input.copyTo(outputStream)
                    }
                } ?: throw IOException("Failed to open output stream for MediaStore")

                // IS_PENDING 플래그 해제
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

            } else {
                // ✅ Android 9 이하 (legacy 저장)
                @Suppress("DEPRECATION")
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val file = java.io.File(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    body.byteStream().use { input ->
                        input.copyTo(outputStream)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("SettingsViewModel", "Failed to write file", e)
            if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.delete(uri, null, null)
            }
            throw e
        } finally {
            body.close()
        }
    }

    /**
     * UI 메시지 초기화
     */
    fun clearMessages() {
        _state.value = SettingsUiState()
    }
}
