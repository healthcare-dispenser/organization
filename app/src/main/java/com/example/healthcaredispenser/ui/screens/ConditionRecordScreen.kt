@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.healthcaredispenser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination // ⭐️ 추가
import androidx.navigation.NavController
import com.example.healthcaredispenser.navigation.Routes
import com.example.healthcaredispenser.ui.components.BottomBar
import com.example.healthcaredispenser.ui.theme.BorderGray
import com.example.healthcaredispenser.ui.theme.LoginGreen
import com.example.healthcaredispenser.ui.theme.SignBg

// 디자인 스펙에 맞춘 컴포저블
@Composable
fun ConditionRecordScreen(
    navController: NavController,
    profileId: Long // ⭐️ 1. profileId 인자 추가
) {
    // ... (상태 관리, 옵션 목록 동일) ...
    var sleepQuality by remember { mutableStateOf(0) }
    var fatigueLevel by remember { mutableStateOf(0) }
    val canRecord = sleepQuality > 0 && fatigueLevel > 0
    val sleepOptions = listOf(
        "1 - 매우 나쁨", "2 - 나쁨", "3 - 보통", "4 - 좋음", "5 - 매우 좋음"
    )
    val fatigueOptions = listOf(
        "1 - 전혀 피곤하지 않음", "2 - 약간 피곤함", "3 - 보통", "4 - 많이 피곤함", "5 - 매우 피곤함"
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            // ... (뒤로가기 버튼 동일) ...
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로", tint = Color.Black)
                }
            }
        },
        bottomBar = {
            // ⬇️ === 2. 수정된 부분 (BottomBar onClick) === ⬇️
            BottomBar(
                currentRoute = Routes.RECORD, // '기록' 탭 활성화 (ConditionRecord는 Record의 하위 단계로 간주)
                onHomeClick = {
                    navController.navigate("${Routes.HOME}/$profileId") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onRecordClick = {
                    // 현재 스택에 RecordScreen이 있다면 거기로 돌아감, 없다면 새로 이동
                    navController.popBackStack(Routes.RECORD_ROUTE.replace("{profileId}", profileId.toString()), inclusive = false)
                    // 만약 popBackStack이 실패하면 (RecordScreen이 스택에 없으면) navigate
                    if (navController.currentDestination?.route != Routes.RECORD_ROUTE) {
                        navController.navigate("${Routes.RECORD}/$profileId") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onSettingsClick = {
                    navController.navigate("${Routes.SETTINGS}/$profileId") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            // ⬆️ ======================================== ⬆️
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {

            Text(
                text = "오늘의 컨디션",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            // 메인 카드
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SignBg)
                    .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // --- 1. 수면의 질 ---
                Text(
                    text = "수면의 질은 어떠셨나요?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sleepOptions.forEachIndexed { index, text ->
                        val optionIndex = index + 1
                        RatingOptionRow(
                            text = text,
                            selected = (sleepQuality == optionIndex),
                            onClick = { sleepQuality = optionIndex }
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // --- 2. 피로도 ---
                Text(
                    text = "피로도는 어떠신가요?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    fatigueOptions.forEachIndexed { index, text ->
                        val optionIndex = index + 1
                        RatingOptionRow(
                            text = text,
                            selected = (fatigueLevel == optionIndex),
                            onClick = { fatigueLevel = optionIndex }
                        )
                    }
                }
            } // End of Column

            Spacer(Modifier.height(24.dp)) // 버튼 위 간격

            // 기록하기 버튼
            Button(
                onClick = {
                    // TODO: 서버로 기록 전송
                    navController.popBackStack() // 이전 화면(RecordScreen)으로 돌아감
                },
                enabled = canRecord,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LoginGreen,
                    disabledContainerColor = LoginGreen.copy(alpha = 0.4f)
                )
            ) {
                Text("기록하기", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(24.dp)) // 바텀바 전 여백
        }
    }
}

/**
 * 라디오 버튼이 있는 옵션 행
 */
@Composable
private fun RatingOptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = LoginGreen,
                unselectedColor = BorderGray
            )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}