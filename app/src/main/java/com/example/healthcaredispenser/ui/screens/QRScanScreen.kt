@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.healthcaredispenser.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.healthcaredispenser.R
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import com.example.healthcaredispenser.data.api.DispenserApi
import com.example.healthcaredispenser.data.api.RegisterDispenserRequest
import com.example.healthcaredispenser.data.api.RetrofitClient
import com.example.healthcaredispenser.data.auth.DispenserStore
import retrofit2.HttpException

@Composable
fun QRScanScreen(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onSave: (String) -> Unit = {}
) {
    val green = Color(0xFF2E7D32)
    val borderGray = Color(0xFFD0D5DD)

    var scanned by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Retrofit
    val api = remember { RetrofitClient.retrofit.create(DispenserApi::class.java) }

    // ZXing 스캐너
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { raw ->
            scanned = normalizeDispenserUuid(raw)
        }
    }

    // 권한
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startScan(scanLauncher) else {
            scope.launch { snackbarHostState.showSnackbar("카메라 권한이 필요합니다.") }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { inner ->
        Column(
            modifier = modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .offset(y = 130.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "디스펜서의 QR코드를 스캔해\n기기를 등록하세요.",
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(BorderStroke(2.dp, green), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.photo_camera),
                    contentDescription = "카메라",
                    tint = Color.Black,
                    modifier = Modifier.size(88.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) startScan(scanLauncher) else permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = green)
            ) {
                Text("QR 코드 스캔하기", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, borderGray),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White, contentColor = Color.Black
                    )
                ) { Text("취소", fontSize = 18.sp) }

                Button(
                    onClick = {
                        val uuid = scanned.trim()
                        if (uuid.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("QR 스캔 후 저장해 주세요.") }
                            return@Button
                        }
                        scope.launch {
                            try {
                                // ✅ 서버 등록 요청 (요청값 대신, 응답 uuid 신뢰)
                                val res = api.register(RegisterDispenserRequest(uuid))
                                val saved = res.dispenserUuid.ifBlank { uuid }
                                DispenserStore.set(context, saved)
                                onSave(saved) // 홈으로 이동
                            } catch (e: HttpException) {
                                val msg = e.response()?.errorBody()?.string()
                                snackbarHostState.showSnackbar("HTTP ${e.code()}: ${msg ?: e.message()}")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("오류: ${e.message ?: "알 수 없는 오류"}")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = scanned.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green)
                ) { Text("저장", color = Color.White, fontSize = 18.sp) }
            }
        }
    }
}

// 카메라 시작 헬퍼
private fun startScan(launcher: androidx.activity.compose.ManagedActivityResultLauncher<ScanOptions, com.journeyapps.barcodescanner.ScanIntentResult>) {
    val options = ScanOptions()
        .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        .setPrompt("QR 코드를 프레임 안에 맞춰주세요")
        .setBeepEnabled(true)
        .setOrientationLocked(true)
        .setCaptureActivity(com.example.healthcaredispenser.qr.CustomCaptureActivity::class.java)
    launcher.launch(options)
}

// ✅ 스캔 원문에서 서버가 기대하는 순수 UUID만 추출/정규화
private fun normalizeDispenserUuid(raw: String): String {
    val t = raw.trim()

    // URL 형태라면 ?uuid= 파라미터 우선
    if (t.startsWith("http", ignoreCase = true)) {
        return runCatching {
            Uri.parse(t).getQueryParameter("uuid")?.trim().takeUnless { it.isNullOrEmpty() }
                ?: t.substringAfterLast('/').trim() // 마지막 경로 조각
        }.getOrDefault(t)
    }

    // 텍스트에 uuid= 가 포함된 경우
    if ("uuid=" in t) {
        val v = t.substringAfter("uuid=").substringBefore('&').trim()
        if (v.isNotEmpty()) return v
    }

    // 공백/제어문자 제거
    return t
}