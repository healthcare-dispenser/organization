// app/src/main/java/com/example/healthcaredispenser/App.kt
package com.example.healthcaredispenser

import android.app.Application
import com.example.healthcaredispenser.data.auth.TokenStore

/**
 * 앱 전역 Application
 * - TokenStore 초기화 (JWT 저장/조회)
 * - 필요 시 전역 Context 접근용 instance 제공
 */
class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        TokenStore.init(this)
    }
}
