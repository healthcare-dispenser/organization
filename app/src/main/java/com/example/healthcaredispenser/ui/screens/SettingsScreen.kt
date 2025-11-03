@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.healthcaredispenser.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.healthcaredispenser.R
import com.example.healthcaredispenser.navigation.Routes
import com.example.healthcaredispenser.ui.auth.AuthViewModel
import com.example.healthcaredispenser.ui.components.BottomBar
import com.example.healthcaredispenser.ui.theme.HintGray
import kotlinx.coroutines.launch
import com.example.healthcaredispenser.data.repository.DispenserRepository
import com.example.healthcaredispenser.data.auth.DispenserStore

@Composable
fun SettingsScreen(
    navController: NavController,
    profileId: Long,
    authVm: AuthViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    // ✅ DataStore에서 QR 스캔으로 저장된 uuid 실시간 구독
    val dispenserUuid by DispenserStore.flow(ctx).collectAsState(initial = null)

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
                subtitle = "선택된 생활 습관",
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
                SettingsButton("프로필 수정하기") {
                    navController.navigate("${Routes.HABITS}?profileId=$profileId")
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. 세척 카드 (알림 설정 → 세척)
            SettingsCard(
                iconPainter = painterResource(id = R.drawable.water_drop),
                title = "세척",
                subtitle = "세척 할 용기를 선택해 주세요",
                iconOffsetY = (-12.5).dp
            ) {
                WashSlotRow(
                    onTap = { slot ->
                        val uuid = dispenserUuid
                        if (uuid.isNullOrBlank()) {
                            Toast.makeText(ctx, "먼저 기기를 등록(QR 스캔)해 주세요.", Toast.LENGTH_SHORT).show()
                            navController.navigate(Routes.QRSCAN)
                            return@WashSlotRow
                        }
                        scope.launch {
                            try {
                                DispenserRepository.wash(uuid, slot)
                                Toast.makeText(ctx, "세척 요청 완료 (슬롯 $slot)", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    ctx,
                                    "세척 요청 실패: ${e.message ?: "알 수 없는 오류"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // 3. 데이터 관리 카드
            SettingsCard(
                iconPainter = painterResource(id = R.drawable.bar_chart_4_bars),
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
                iconPainter = painterResource(id = R.drawable.qr_code_scanner),
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
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("로그아웃", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(24.dp))
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
    iconOffsetY: Dp = 0.dp,
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
                    modifier = Modifier.offset(y = iconOffsetY)
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
                        .offset(y = iconOffsetY)
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
    val borderColor = Color(0xFF6F7783)
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
        border = BorderStroke(1.dp, borderColor) // ✅ 테두리 6F7783로 통일
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

/** ===== 세척용 컴포저블 ===== */

@Composable
private fun WashSlotRow(
    onTap: (Int) -> Unit
) {
    val gap = 12.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        (1..4).forEach { slot ->
            WashBox(
                label = slot.toString(),
                onClick = { onTap(slot) }
            )
            if (slot != 4) Spacer(Modifier.width(gap))
        }
    }
}

@Composable
private fun WashBox(
    label: String,
    onClick: () -> Unit
) {
    val borderColor = Color(0xFF6F7783)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
