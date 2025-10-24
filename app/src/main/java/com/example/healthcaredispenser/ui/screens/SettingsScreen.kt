@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.healthcaredispenser.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.healthcaredispenser.R
import com.example.healthcaredispenser.navigation.Routes
import com.example.healthcaredispenser.ui.auth.AuthViewModel
import com.example.healthcaredispenser.ui.components.BottomBar
import com.example.healthcaredispenser.ui.theme.BorderGray
import com.example.healthcaredispenser.ui.theme.HintGray
import com.example.healthcaredispenser.ui.theme.SignBg

@Composable
fun SettingsScreen(
    navController: NavController,
    profileId: Long,
    authVm: AuthViewModel = viewModel()
) {
    Scaffold(
        containerColor = Color.White,


        bottomBar = {
            BottomBar(
                currentRoute = Routes.SETTINGS, // "설정" 탭 활성화
                onHomeClick = {
                    navController.navigate("${Routes.HOME}/$profileId") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onRecordClick = {
                    navController.navigate("${Routes.RECORD}/$profileId") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onSettingsClick = { /* 현재 화면 */ }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(30.dp))
            Text(
                text = "설정",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            // 1. 프로필 카드
            SettingsCard(
                iconPainter = painterResource(id = R.drawable.person),
                title = "프로필",
                subtitle = "선택된 생활 습관" ,
                iconOffsetY = (-12).dp

            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("4개 태그", fontSize = 14.sp, color = HintGray) // TODO: 동적 데이터
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("4", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))

                // ⬇️ === 수정된 부분 === ⬇️
                SettingsButton("프로필 수정하기") {
                    // ✅ 수정: '생성' 플로우(습관 선택)로 'profileId'를 가지고 이동
                    // NavGraph의 "habits?profileId={profileId}" 경로를 호출
                    navController.navigate("${Routes.HABITS}?profileId=$profileId")
                }
                // ⬆️ =================== ⬆️
            }

            Spacer(Modifier.height(16.dp))

            // 2. 알림 설정 카드
            SettingsCard(
                iconPainter = painterResource(id = R.drawable.notifications),
                title = "알림 설정",
                subtitle = "취침 전, 운동일 알림 설정",
                iconOffsetY = (-11).dp
            ) {
                SettingsButton("알림 시간 설정") {
                    // TODO: 알림 설정 화면으로 이동
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3. 데이터 관리 카드
            SettingsCard(
                iconPainter = painterResource(id = R.drawable.bar_chart_4_bars), // RecordScreen 아이콘
                title = "데이터 관리",
                subtitle = null
            ) {
                SettingsButton(
                    text = "데이터 내보내기",
                    icon = Icons.Default.Download
                ) {
                    // TODO: 데이터 내보내기 로직
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. 기기 등록 카드
            SettingsCard(
                iconPainter = painterResource(id = R.drawable.qr_code_scanner), // QRScanScreen 아이콘
                title = "기기 등록",
                subtitle = null,
                iconOffsetY = 1.dp
            ) {
                SettingsButton("QR코드 스캔하기") {
                    navController.navigate(Routes.QRSCAN)
                }
            }

            Spacer(Modifier.height(32.dp))

            // 5. 로그아웃 버튼
            Button(
                onClick = {
                    authVm.logout()
                    // 로그인 화면으로 이동, 백스택 모두 제거
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) // 빨간색
            ) {
                Text("로그아웃", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(24.dp)) // 바텀바 전 여백
        }
    }
}


// --- 이 파일 내에서만 사용하는 Helper Composables ---
@Composable
private fun SettingsCard(
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    title: String,
    subtitle: String?,
    iconOffsetY: Dp = 0.dp, // ✅ 추가: 아이콘 y 오프셋
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF6F7783), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = Color.Black,
                    modifier = Modifier.offset(y = iconOffsetY) // ✅ offset 적용
                )
                Spacer(Modifier.width(12.dp))
            }
            if (iconPainter != null) {
                Icon(
                    iconPainter,
                    contentDescription = title,
                    tint = Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                        .offset(y = iconOffsetY) // ✅ offset 적용
                )
                Spacer(Modifier.width(12.dp))
            }

            Column {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, fontSize = 13.sp, color = HintGray)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        content()
    }
}


@Composable
private fun SettingsButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}