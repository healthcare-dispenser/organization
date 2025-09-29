package com.example.healthcaredispenser.data.api

import com.example.healthcaredispenser.data.network.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // ✅ EC2 Nginx 프록시: 80 → 8080, 끝에 반드시 /
    private const val BASE_URL = "http://54.180.94.196/"

    private val logging by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 개발 단계는 BODY
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            // JWT 자동 부착 (로그인/회원가입 엔드포인트는 제외)
            .addInterceptor(AuthInterceptor())
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}