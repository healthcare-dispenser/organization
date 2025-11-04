package com.example.healthcaredispenser.data.api

import com.example.healthcaredispenser.data.network.AuthInterceptor
import okhttp3.Interceptor // 👈 1. 이 import를 추가하세요
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // ✅ EC2 Nginx 프록시: 80 → 8080, 끝에 반드시 /
    private const val BASE_URL = "http://35.208.61.223/"

    private val logging by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 개발 단계는 BODY
        }
    }

    // ⬇️ === 2. 헤더를 관리하는 인터셉터 새로 추가 === ⬇️
    // (이게 모든 요청을 검사해서 알맞은 Accept 헤더를 붙여줍니다)
    private val acceptHeaderInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // 요청 URL에 ".csv"가 포함되어 있는지 확인
        if (originalRequest.url.toString().endsWith(".csv")) {
            // CSV 요청: 'Accept' 헤더를 'text/csv'로 강제 설정
            val newRequest = originalRequest.newBuilder()
                .header("Accept", "text/csv") // .header()는 기존 것을 덮어씁니다.
                .build()
            chain.proceed(newRequest)
        } else {
            // CSV 아닌 모든 요청: 'Accept' 헤더를 'application/json'으로 강제 설정
            val newRequest = originalRequest.newBuilder()
                .header("Accept", "application/json") // .header()는 기존 것을 덮어씁니다.
                .build()
            chain.proceed(newRequest)
        }
    }
    // ⬆️ ======================================== ⬆️


    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            // JWT 자동 부착 (이건 건드리지 않음)
            .addInterceptor(AuthInterceptor())

            // ✅ 3. 새로 만든 헤더 인터셉터를 Auth *다음에* 추가
            .addInterceptor(acceptHeaderInterceptor)

            .addInterceptor(logging) // 로깅은 맨 마지막이 좋음
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