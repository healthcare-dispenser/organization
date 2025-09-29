package com.example.healthcaredispenser.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Context 확장으로 DataStore 생성
private val Context.dataStore by preferencesDataStore("auth_prefs")

class TokenDataStore(private val context: Context) {

    companion object {
        private val KEY_ACCESS = stringPreferencesKey("access_token")
    }

    /** 토큰 읽기 (Flow) */
    fun getToken(): Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_ACCESS] }

    /** 토큰 저장 */
    suspend fun setToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token == null) prefs.remove(KEY_ACCESS)
            else prefs[KEY_ACCESS] = token
        }
    }

    /** 전체 비우기 */
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
