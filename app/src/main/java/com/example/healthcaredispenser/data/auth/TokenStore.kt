package com.example.healthcaredispenser.data.auth

import android.content.Context
import com.example.healthcaredispenser.data.local.TokenDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * JWT 토큰 저장/관리 (DataStore + 메모리 캐시)
 * - Application.onCreate()에서 반드시 init(context) 1회 호출
 * - 저장/삭제는 suspend 함수에서 호출
 * - 읽기(get)는 캐시 즉시 반환 (비블로킹)
 */
object TokenStore {
    private lateinit var dataStore: TokenDataStore
    @Volatile private var cached: String? = null

    private lateinit var _tokenFlow: Flow<String?>
    private var initialized = false

    /** Application.onCreate()에서 호출 */
    fun init(context: Context) {
        if (initialized) return
        dataStore = TokenDataStore(context)

        // Flow 준비
        _tokenFlow = dataStore.getToken()

        // 앱 시작 시 캐시 프리로드
        val appScope = CoroutineScope(Dispatchers.IO)
        appScope.launch {
            cached = dataStore.getToken().first()
        }

        initialized = true
    }

    /** 현재 토큰 즉시 반환 (메모리 캐시) */
    fun get(): String? {
        checkInitialized()
        return cached
    }

    /** 토큰 Flow (UI 옵저빙용) */
    fun flow(): Flow<String?> {
        checkInitialized()
        return _tokenFlow.stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.Lazily,
            initialValue = cached
        )
    }

    /** 토큰 저장 */
    suspend fun set(token: String?) {
        checkInitialized()
        cached = token
        dataStore.setToken(token)
    }

    /** 로그아웃/만료 시 호출 */
    suspend fun clear() {
        checkInitialized()
        cached = null
        dataStore.clear()
    }

    private fun checkInitialized() {
        check(initialized) {
            "TokenStore is not initialized. Call TokenStore.init(context) in Application.onCreate()."
        }
    }
}
