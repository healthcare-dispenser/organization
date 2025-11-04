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
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ✅ tagCount 제거됨
data class SettingsUiState(
    val loading: Boolean = false, // 데이터 내보내기 로딩
    val error: String? = null,
    val successMessage: String? = null
)

// ✅ 생성자에서 ProfileRepository 제거됨
class SettingsViewModel(
    private val exportRepo: ExportRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    // ✅ loadProfile 함수 제거됨

    /**
     * 데이터 내보내기 (CSV 파일 다운로드)
     * ✅ 간단한 .value = SettingsUiState() 방식으로 되돌림
     */
    fun exportData(context: Context) {
        if (_state.value.loading) return
        viewModelScope.launch {
            _state.value = SettingsUiState(loading = true) // 로딩 시작
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
     * ResponseBody를 MediaStore 또는 레거시 방식으로 저장 (변경 없음)
     */
    private fun saveCsvFile(context: Context, body: ResponseBody) {
        // ... (이전과 동일한 파일 저장 로직) ...
        val resolver = context.contentResolver
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "intake-feedback-$timeStamp.csv"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
        }
        var outputStream: OutputStream? = null
        var uri: android.net.Uri? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
                uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IOException("Failed to create MediaStore entry (API 29+)")
                outputStream = resolver.openOutputStream(uri)
                    ?: throw IOException("Failed to open output stream for MediaStore")
                body.byteStream().use { input -> input.copyTo(outputStream) }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = java.io.File(downloadsDir, fileName)
                outputStream = FileOutputStream(file)
                body.byteStream().use { input -> input.copyTo(outputStream) }
            }
        } catch (e: IOException) {
            Log.e("SettingsViewModel", "Failed to write file", e)
            if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.delete(uri, null, null)
            }
            throw e
        } finally {
            outputStream?.close()
            body.close()
        }
    }

    /**
     * UI에 표시된 메시지 초기화 (간단한 방식으로 되돌림)
     */
    fun clearMessages() {
        _state.value = SettingsUiState()
    }
}