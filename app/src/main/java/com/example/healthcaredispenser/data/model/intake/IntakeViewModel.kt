package com.example.healthcaredispenser.ui.intake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcaredispenser.data.api.IntakeApi
import com.example.healthcaredispenser.data.api.RetrofitClient
import com.example.healthcaredispenser.data.model.intake.IntakeItem
import com.example.healthcaredispenser.data.repository.IntakeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class IntakeViewModel : ViewModel() {

    private val api: IntakeApi = RetrofitClient.retrofit.create(IntakeApi::class.java)
    private val repo = IntakeRepository(api)

    private val _all = MutableStateFlow<List<IntakeItem>>(emptyList())
    val all: StateFlow<List<IntakeItem>> = _all

    private val _recent4 = MutableStateFlow<List<IntakeItem>>(emptyList())
    val recent4: StateFlow<List<IntakeItem>> = _recent4

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load(profileId: Long, dispenserUuid: String = "") {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                // 신 엔드포인트 우선, 실패 시 구 엔드포인트로 폴백
                val res = repo.listIntakesSmart(profileId, dispenserUuid)

                // 최신순 (completedAt > requestedAt)
                val sorted = res.items.sortedByDescending { it.completedAt ?: it.requestedAt ?: "" }
                _all.value = sorted
                _recent4.value = sorted.take(4)
            } catch (e: Exception) {
                _error.value = e.message
                _all.value = emptyList()
                _recent4.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    /** 서버 포맷(오프셋/초/소수초 유무 상관없이) → "HH:mm" */
    fun toUiTime(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val out = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        // 1️⃣ 오프셋(예: +09:00, Z 등)이 있는 경우
        try {
            val odt = OffsetDateTime.parse(iso)
            return odt.toLocalDateTime().format(out)
        } catch (_: DateTimeParseException) { /* 다음 시도 */ }

        // 2️⃣ 오프셋 없는 ISO_LOCAL_DATE_TIME (예: 2025-10-25T12:08:17.349703)
        try {
            val ldt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            return ldt.format(out)
        } catch (_: DateTimeParseException) { /* 다음 시도 */ }

        // 3️⃣ 최후: 정규식으로 yyyy-MM-dd와 HH:mm 추출
        val m = Regex("""(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2})""").find(iso)
        if (m != null) return "${m.groupValues[1]} ${m.groupValues[2]}"

        // 그래도 실패 시 원문 반환 (디버깅용)
        return iso
    }

    /** 성분 요약 문자열 생성 */
    fun buildSummary(item: IntakeItem): String {
        val parts = mutableListOf<String>()
        item.melatonin?.let { parts += "멜라토닌 ${trimZero(it)}mg" }
        item.magnesium?.let { parts += "마그네슘 ${trimZero(it)}mg" }
        item.electrolyte?.let { parts += "전해질 ${trimZero(it)}mg" }
        item.vitamin?.let { parts += "아연 ${trimZero(it)}mg" }
        return parts.joinToString(" , ")
    }

    private fun trimZero(v: Double): String =
        if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
}
