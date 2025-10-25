package com.example.healthcaredispenser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.healthcaredispenser.R
import com.example.healthcaredispenser.navigation.Routes
import com.example.healthcaredispenser.ui.components.BottomBar
import com.example.healthcaredispenser.ui.intake.IntakeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

private val OutlineGray  = Color(0xFF6F7783)
private val TextPrimary  = Color(0xFF1A1A1A)
private val TextSecondary= Color(0xFF6F7783)
private val ItemBG       = Color(0xFFE8F5E9)
private val PageBG       = Color(0xFFFFFFFF)

@Composable
fun IntakeHistoryScreen(
    navController: NavHostController,
    profileId: Long
) {
    val vm: IntakeViewModel = viewModel()
    val items by vm.all.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(profileId) { vm.load(profileId) }

    Scaffold(
        bottomBar = {
            BottomBar(
                currentRoute = Routes.RECORD,
                onHomeClick = {
                    navController.navigate("${Routes.HOME}/$profileId") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onRecordClick = { /* 현재 탭 */ },
                onSettingsClick = {
                    navController.navigate("${Routes.SETTINGS}/$profileId") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        containerColor = PageBG
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)               // 상하단 시스템/바텀바 패딩 반영
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // 뒤로가기
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_2),
                    contentDescription = "뒤로",
                    tint = TextPrimary
                )
            }

            // 제목
            Text(
                text = "복용 기록",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            when {
                loading -> Text("불러오는 중...", color = TextSecondary)
                error != null -> Text("기록을 불러오지 못했어요", color = Color(0xFFD32F2F))
                items.isEmpty() -> EmptyIntakeView(onGoBack = { navController.popBackStack() })
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        // ✅ 바텀바(72dp) + 여유(16dp) = 88dp, 고정 패딩으로 충돌/오류 없이 처리
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 50.dp
                        )
                    ) {
                        items(items, key = { it.intakeId }) { log ->
                            IntakeHistoryItem(
                                timeText = vm.toUiTime(log.completedAt ?: log.requestedAt),
                                summary  = vm.buildSummary(log),
                                onClick  = { /* 상세 연결 필요 시 */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntakeHistoryItem(
    timeText: String,
    summary: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ItemBG, RoundedCornerShape(12.dp))
            .border(1.dp, SolidColor(OutlineGray), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(text = timeText, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(text = summary, color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun EmptyIntakeView(
    onGoBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Icon(
            painter = painterResource(id = R.drawable.insert_chart),
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("복용 기록이 없어요", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text("섭취를 시작하면 여기에 기록이 표시돼요.", color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onGoBack,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor   = Color.Black
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineGray)
        ) { Text("돌아가기") }
        Spacer(Modifier.height(24.dp))
        Divider(color = OutlineGray.copy(alpha = 0.3f))
    }
}
