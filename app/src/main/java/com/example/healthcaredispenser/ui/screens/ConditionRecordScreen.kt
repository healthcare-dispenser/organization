@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.healthcaredispenser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // ⭐️ 스크롤을 위해 추가
import androidx.compose.foundation.selection.selectable // ⭐️ 라디오 버튼을 위해 추가
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll // ⭐️ 스크롤을 위해 추가
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role // ⭐️ 라디오 버튼을 위해 추가
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.healthcaredispenser.navigation.Routes
import com.example.healthcaredispenser.ui.components.BottomBar
import com.example.healthcaredispenser.ui.theme.BorderGray
import com.example.healthcaredispenser.ui.theme.LoginGreen
import com.example.healthcaredispenser.ui.theme.SignBg

// 디자인 스펙에 맞춘 컴포저블
@Composable
fun ConditionRecordScreen(
    navController: NavController
) {
    // 1. 상태 관리
    var sleepQuality by remember { mutableStateOf(0) } // 0: 선택 안 함, 1-5: 선택
    var fatigueLevel by remember { mutableStateOf(0) }

    val canRecord = sleepQuality > 0 && fatigueLevel > 0

    // 2. 옵션 목록
    val sleepOptions = listOf(
        "1 - 매우 나쁨", "2 - 나쁨", "3 - 보통", "4 - 좋음", "5 - 매우 좋음"
    )
    val fatigueOptions = listOf(
        "1 - 전혀 피곤하지 않음", "2 - 약간 피곤함", "3 - 보통", "4 - 많이 피곤함", "5 - 매우 피곤함"
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            // 뒤로가기 버튼
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
            BottomBar(
                currentRoute = Routes.RECORD, // '기록' 탭 활성화
                onHomeClick = {
                    navController.popBackStack(Routes.PROFILE, false) // 임시
                },
                onRecordClick = { /* 현재 화면 */ },
                onSettingsClick = { /* TODO */ }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding) // 상단/하단 바 영역 피하기
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // ⭐️ Column 전체 스크롤 적용
                .padding(horizontal = 20.dp), // 좌우 여백
        ) {

            Text(
                text = "오늘의 컨디션",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp)) // ⭐️ 원래 간격

            // 메인 카드
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // .weight(1f) 제거 (스크롤을 위해)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SignBg)
                    .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 24.dp) // ⭐️ 원래 간격
            ) {
                // --- 1. 수면의 질 ---
                Text(
                    text = "수면의 질은 어떠셨나요?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(Modifier.height(12.dp)) // ⭐️ 원래 간격
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { // ⭐️ 원래 간격
                    sleepOptions.forEachIndexed { index, text ->
                        val optionIndex = index + 1
                        RatingOptionRow( // ⭐️ 라디오 버튼 버전
                            text = text,
                            selected = (sleepQuality == optionIndex),
                            onClick = { sleepQuality = optionIndex }
                        )
                    }
                }

                Spacer(Modifier.height(28.dp)) // ⭐️ 원래 간격

                // --- 2. 피로도 ---
                Text(
                    text = "피로도는 어떠신가요?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(Modifier.height(12.dp)) // ⭐️ 원래 간격
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { // ⭐️ 원래 간격
                    fatigueOptions.forEachIndexed { index, text ->
                        val optionIndex = index + 1
                        RatingOptionRow( // ⭐️ 라디오 버튼 버전
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
                    navController.popBackStack()
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
 * ⭐️ 라디오 버튼이 있는 옵션 행
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
            .padding(vertical = 4.dp), // ⭐️ 원래 간격
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
            fontSize = 16.sp // ⭐️ 원래 폰트 크기
        )
    }
}