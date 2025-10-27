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
import java.time.*
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
                // 서버 기본 size 제한(예: 7) 회피: size=200 명시
                val res = repo.listIntakesSmart(profileId, dispenserUuid, size = 200)

                // 정렬 키: completedAt이 있으면 그 시간, 없으면 requestedAt
                val sorted = res.items.sortedByDescending {
                    maxOf(parseSortKey(it.completedAt), parseSortKey(it.requestedAt))
                }

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

    /** 서버 ISO 문자열(오프셋/소수초 유무 무관) → "yyyy-MM-dd HH:mm" */
    fun toUiTime(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val out = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        // 1) 오프셋 있는 ISO (예: 2025-10-25T12:08:17.349703+09:00)
        try {
            val odt = OffsetDateTime.parse(iso)
            return odt.toLocalDateTime().format(out)
        } catch (_: DateTimeParseException) { }

        // 2) 오프셋 없는 ISO_LOCAL_DATE_TIME (예: 2025-10-25T12:08:17.349703)
        try {
            val ldt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            return ldt.format(out)
        } catch (_: DateTimeParseException) { }

        // 3) yyyy-MM-ddTHH:mm 패턴만 추출
        val m = Regex("""(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2})""").find(iso)
        if (m != null) return "${m.groupValues[1]} ${m.groupValues[2]}"

        // 실패 시 원문 반환(디버깅)
        return iso
    }

    /** 성분 요약 문자열 생성 */
    fun buildSummary(item: IntakeItem): String {
        val parts = mutableListOf<String>()
        item.melatonin?.let { parts += "멜라토닌 ${trimZero(it)}mg" }
        item.magnesium?.let { parts += "마그네슘 ${trimZero(it)}mg" }
        item.electrolyte?.let { parts += "전해질 ${trimZero(it)}mg" }
        item.zinc?.let { parts += "아연 ${trimZero(it)}mg" }
        return parts.joinToString(" , ")
    }

    private fun trimZero(v: Double): String =
        if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()

    /** 정렬 신뢰도 향상을 위한 파서: epoch milli 반환 (실패 시 Long.MIN_VALUE) */
    private fun parseSortKey(iso: String?): Long {
        if (iso.isNullOrBlank()) return Long.MIN_VALUE
        return try {
            // Offset 포함 문자열
            OffsetDateTime.parse(iso).toInstant().toEpochMilli()
        } catch (_: DateTimeParseException) {
            try {
                // Offset 없는 LocalDateTime → 시스템 타임존 기준
                val ldt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: DateTimeParseException) {
                // yyyy-MM-ddTHH:mm만 있는 경우
                val m = Regex("""(\d{4}-\d{2}-\d{2})T(\d{2}):(\d{2})""").find(iso)
                if (m != null) {
                    val dt = LocalDateTime.parse(
                        "${m.groupValues[1]}T${m.groupValues[2]}:${m.groupValues[3]}:00",
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    )
                    dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                } else Long.MIN_VALUE
            }
        }
    }
}
