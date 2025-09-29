plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.healthcaredispenser"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.healthcaredispenser"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // ⬇️ Compose 활성화
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Compose BOM (버전 한번에 관리)
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))

    // 기본 Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.9.2")

    // 디버그 도구
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Compose 아이콘 확장
    implementation("androidx.compose.material:material-icons-extended:1.6.8")


    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // (Compose에서 ViewModel 쓰면 필요)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.2")

}
