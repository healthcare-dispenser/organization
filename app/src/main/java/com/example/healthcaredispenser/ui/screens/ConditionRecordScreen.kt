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
import androidx.compose.ui.platform.LocalLifecycleOwner // ⭐️ 추가
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle // ⭐️ 추가
import androidx.lifecycle.flowWithLifecycle // ⭐️ 추가
import androidx.lifecycle.viewmodel.compose.viewModel // ⭐️ 추가
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavController
import com.example.healthcaredispenser.navigation.Routes
import com.example.healthcaredispenser.ui.components.BottomBar
import com.example.healthcaredispenser.ui.condition.ConditionNavigationEvent // ⭐️ 추가
import com.example.healthcaredispenser.ui.condition.ConditionViewModel // ⭐️ 추가
import com.example.healthcaredispenser.ui.theme.BorderGray
import com.example.healthcaredispenser.ui.theme.LoginGreen
import com.example.healthcaredispenser.ui.theme.SignBg

// 디자인 스펙에 맞춘 컴포저블
@Composable
fun ConditionRecordScreen(
    navController: NavController,
    profileId: Long,
    vm: ConditionViewModel = viewModel() // ⭐️ 1. ViewModel 주입
) {
    // ⭐️ 2. UI 상태 및 이벤트 수집
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() } // 에러 메시지 표시용

    // ⭐️ 3. Navigation Event 처리 (뒤로 가기)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(vm.navigationEvent, lifecycleOwner.lifecycle) {
        vm.navigationEvent.flowWithLifecycle(
            lifecycleOwner.lifecycle,
            Lifecycle.State.STARTED
        ).collect { event ->
            when (event) {
                is ConditionNavigationEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    // ⭐️ 4. 에러 메시지 표시
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError() // 스낵바 표시 후 에러 상태 초기화
        }
    }


    // 상태 관리, 옵션 목록
    var sleepQuality by remember { mutableStateOf(0) }
    var fatigueLevel by remember { mutableStateOf(0) }
    val canRecord = sleepQuality > 0 && fatigueLevel > 0 && !uiState.isLoading // ⭐️ 로딩 중 아닐 때만 가능
    val sleepOptions = listOf(
        "1 - 매우 나쁨", "2 - 나쁨", "3 - 보통", "4 - 좋음", "5 - 매우 좋음"
    )
    val fatigueOptions = listOf(
        "1 - 전혀 피곤하지 않음", "2 - 약간 피곤함", "3 - 보통", "4 - 많이 피곤함", "5 - 매우 피곤함"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // ⭐️ 스낵바 호스트 추가
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
                currentRoute = Routes.RECORD,
                onHomeClick = {
                    navController.navigate("${Routes.HOME}/$profileId") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onRecordClick = {
                    navController.popBackStack(Routes.RECORD_ROUTE.replace("{profileId}", profileId.toString()), inclusive = false)
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // ⭐️ 스크롤 가능하게 유지
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
                    // ✅ ViewModel 함수 호출하여 기록 저장 요청
                    vm.saveConditionRecord(profileId, sleepQuality, fatigueLevel)
                },
                enabled = canRecord, // ⭐️ 로딩 중 아닐 때 활성화
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LoginGreen,
                    disabledContainerColor = LoginGreen.copy(alpha = 0.4f)
                )
            ) {
                // ⭐️ 로딩 상태 표시
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("기록하기", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(24.dp)) // 바텀바 전 여백
        }
    }
}

// 라디오 버튼이 있는 옵션 행
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