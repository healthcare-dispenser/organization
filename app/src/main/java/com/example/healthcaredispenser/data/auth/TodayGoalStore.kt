package com.example.healthcaredispenser.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.goalDataStore by preferencesDataStore(name = "today_goal")

object TodayGoalStore {
    private val fmt = DateTimeFormatter.BASIC_ISO_DATE // yyyyMMdd
    private fun todayId(): String = LocalDate.now().format(fmt)

    private fun keys(profileId: Long): Pair<androidx.datastore.preferences.core.Preferences.Key<Int>, androidx.datastore.preferences.core.Preferences.Key<String>> {
        val countKey = intPreferencesKey("goal_count_$profileId")
        val dateKey  = stringPreferencesKey("goal_date_$profileId")
        return countKey to dateKey
    }

    /** (count, savedDayId) 를 해당 프로필로 스트림 제공 */
    fun flow(context: Context, profileId: Long) = context.goalDataStore.data.map { pref ->
        val (K_COUNT, K_DATE) = keys(profileId)
        val count = pref[K_COUNT] ?: 0
        val dayId = pref[K_DATE] ?: todayId()
        count to dayId
    }

    /** 저장된 날짜가 오늘과 다르면 0으로 초기화 (해당 프로필만) */
    suspend fun ensureToday(context: Context, profileId: Long) {
        val nowId = todayId()
        val (K_COUNT, K_DATE) = keys(profileId)
        val cur = context.goalDataStore.data.first()
        val savedId = cur[K_DATE]
        if (savedId == null || savedId != nowId) {
            context.goalDataStore.edit {
                it[K_COUNT] = 0
                it[K_DATE] = nowId
            }
        }
    }

    /** 0~2로 클램프하여 설정 (해당 프로필만) */
    suspend fun setCount(context: Context, profileId: Long, count: Int) {
        val (K_COUNT, K_DATE) = keys(profileId)
        val clamped = count.coerceIn(0, 2)
        context.goalDataStore.edit {
            it[K_COUNT] = clamped
            it[K_DATE] = todayId()
        }
    }

    /** +1 (최대 2) (해당 프로필만) */
    suspend fun increment(context: Context, profileId: Long) {
        ensureToday(context, profileId)
        val (cnt, _) = flow(context, profileId).first()
        setCount(context, profileId, (cnt + 1).coerceAtMost(2))
    }

    /** 오늘자 0으로 리셋 (해당 프로필만) */
    suspend fun resetForToday(context: Context, profileId: Long) {
        val (K_COUNT, K_DATE) = keys(profileId)
        context.goalDataStore.edit {
            it[K_COUNT] = 0
            it[K_DATE] = todayId()
        }
    }
}
