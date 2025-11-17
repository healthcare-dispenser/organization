package com.example.healthcaredispenser.data.api

import com.example.healthcaredispenser.data.network.AuthInterceptor
import okhttp3.Interceptor // 👈 import 확인
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // ✅ IP 주소 확인!
    private const val BASE_URL = "http://35.209.162.103/"

    private val logging by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // ⬇️ === 1. 헤더 관리 인터셉터 추가 (CSV 요청만 헤더 변경) === ⬇️
    private val acceptHeaderInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // ❌ 기존: .endsWith(".csv") -> 파라미터(?profileId=...) 때문에 실패함
        // ✅ 수정: .contains(".csv") -> 주소 중간에 .csv가 있어도 인식함!
        if (originalRequest.url.toString().contains(".csv")) {
            val newRequest = originalRequest.newBuilder()
                .header("Accept", "text/csv")
                .build()
            chain.proceed(newRequest)
        } else {
            val newRequest = originalRequest.newBuilder()
                .header("Accept", "application/json")
                .build()
            chain.proceed(newRequest)
        }
    }
    // ⬆️ ==================================================== ⬆️

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            // ✅ 2. 여기에 인터셉터 추가
            .addInterceptor(acceptHeaderInterceptor)
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