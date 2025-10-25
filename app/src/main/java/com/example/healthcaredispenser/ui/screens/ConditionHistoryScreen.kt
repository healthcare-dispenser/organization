@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.healthcaredispenser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext // ⭐️ Toast용 (선택)
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // ⭐️ 추가
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // ⭐️ 추가
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.healthcaredispenser.data.model.condition.ConditionRecordResponseItem // ⭐️ 모델 import
import com.example.healthcaredispenser.navigation.Routes
import com.example.healthcaredispenser.ui.components.BottomBar
import com.example.healthcaredispenser.ui.condition.ConditionViewModel // ⭐️ ViewModel import
import com.example.healthcaredispenser.ui.theme.BorderGray
import com.example.healthcaredispenser.ui.theme.HintGray
import com.example.healthcaredispenser.ui.theme.LoginGreen
import com.example.healthcaredispenser.ui.theme.SignBg
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException // ⭐️ 추가

// data class ConditionRecordItem 제거 (ViewModel에서 가져옴)

@Composable
fun ConditionHistoryScreen(
    navController: NavController,
    profileId: Long,
    vm: ConditionViewModel = viewModel() // ⭐️ 1. ViewModel 주입
) {
    // ⭐️ 2. UI 상태 수집
    val uiState by vm.uiState.collectAsState()
    val historyList = uiState.conditionHistory

    // ⭐️ 3. 화면 진입 시 데이터 로딩 (한 번만)
    LaunchedEffect(profileId) {
        vm.fetchConditionHistory(profileId)
    }

    // ⭐️ (선택) 에러 발생 시 Toast 메시지 표시
    val context = LocalContext.current
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // Toast.makeText(context, it, Toast.LENGTH_SHORT).show() // 필요 시 주석 해제
            vm.clearError() // 에러 상태 초기화
        }
    }


    Scaffold(
        containerColor = Color.White,
        topBar = {
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
                    navController.navigate("${Routes.HOME}/$profileId") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onRecordClick = {
                    // 현재 화면이므로 동작 없음
                },
                onSettingsClick = {
                    navController.navigate("${Routes.SETTINGS}/$profileId") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = "컨디션 기록 목록",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            // 로딩 중 표시
            if (uiState.isLoading && historyList.isEmpty()) { // 첫 로딩 시에만 전체 화면 로딩 표시
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // 데이터 없을 때 표시
            else if (!uiState.isLoading && historyList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("기록된 컨디션 정보가 없습니다.", color = HintGray, textAlign = TextAlign.Center) // ⭐️ 문구 추가
                }
            }
            // 데이터 있을 때 목록 표시
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyList) { record -> // ⭐️ dummyHistory -> historyList
                        HistoryItemCard(record = record) // ⭐️ 모델 타입 변경됨
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(record: ConditionRecordResponseItem) {
    // ⭐️ 출력할 날짜 형식 정의 (yy/MM/dd)
    val outputDateFormatter = DateTimeFormatter.ofPattern("yy/MM/dd")

    // ⭐️ 서버에서 받은 ISO 형식 날짜/시간 문자열 파싱 (시간대 정보 포함)
    val dateText = try {
        // OffsetDateTime으로 파싱 (시간대 정보 처리, API 26+)
        val offsetDateTime = OffsetDateTime.parse(record.recordDate)
        // LocalDate로 변환 후 원하는 형식으로 포맷
        offsetDateTime.toLocalDate().format(outputDateFormatter)
    } catch (e: DateTimeParseException) {
        // 파싱 실패 시 날짜 부분만 표시 시도 (T 앞부분)
        record.recordDate.substringBefore("T", record.recordDate)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SignBg.copy(alpha = 0.6f))
            .border(1.dp, BorderGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 날짜
        Text(
            text = dateText, // ⭐️ 파싱/포맷된 날짜 사용
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        // 수면 점수 + 피로도 점수 (변경 없음)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ScoreItem(icon = Icons.Outlined.NightsStay, score = record.sleepQuality)
            ScoreItem(icon = Icons.Outlined.SentimentDissatisfied, score = record.fatigueLevel)
        }
    }
}

@Composable
private fun ScoreItem(icon: ImageVector, score: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = HintGray, modifier = Modifier.size(20.dp))
        Text(
            text = score.toString(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}