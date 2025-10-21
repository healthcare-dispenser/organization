package com.example.healthcaredispenser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthcaredispenser.R
import com.example.healthcaredispenser.navigation.Routes
import com.example.healthcaredispenser.ui.components.BottomBar
import com.example.healthcaredispenser.data.api.IntakeApi
import com.example.healthcaredispenser.data.api.RetrofitClient
import com.example.healthcaredispenser.data.repository.IntakeRepository
import com.example.healthcaredispenser.ui.home.HomeViewModel
import com.example.healthcaredispenser.data.auth.DispenserStore
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun HomeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRecord: () -> Unit,
    onNavigateToSettings: () -> Unit,
    profileId: Long
) {
    // VM
    val vm = remember {
        val retrofit = RetrofitClient.retrofit
        val api = retrofit.create(IntakeApi::class.java)
        val repo = IntakeRepository(api)
        HomeViewModel(repo)
    }
    val ui = vm.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()   // 🔸 onClick에서 코루틴 사용

    // 저장된 디스펜서 UUID (DataStore)
    val dispenserUuid by DispenserStore.flow(context).collectAsState(initial = null)

    // 상태 피드백
    LaunchedEffect(ui.value.status) {
        when (ui.value.status) {
            "SUCCESS" -> {
                snackbarHostState.showSnackbar("배출 완료!")
                // onNavigateToRecord()
            }
            "FAIL" -> snackbarHostState.showSnackbar("배출 실패. 잠시 후 다시 시도해 주세요.")
        }
    }
    LaunchedEffect(ui.value.error) {
        ui.value.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomBar(
                currentRoute = Routes.HOME,
                onHomeClick = { /* 이미 홈 */ },
                onRecordClick = onNavigateToRecord,
                onSettingsClick = onNavigateToSettings
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .offset(y = 34.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "좋은 아침이에요!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "오늘의 맞춤 배합을 확인해보세요!",
                color = Color(0xFF6F7783),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(16.dp))

            // 오늘의 목표 박스
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFDF7), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF6F7783), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("오늘의 목표", fontWeight = FontWeight.Bold)
                            Text("건강한 하루를 위한 첫 걸음", color = Color(0xFF6F7783), fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.schedule),
                                contentDescription = "schedule",
                                tint = Color(0xFF000000)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("0/3", color = Color(0xFF000000))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color(0xFFEAEFE3), RoundedCornerShape(4.dp))
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 오늘의 추천 배합 박스
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF6F7783), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.trending_up),
                            contentDescription = "오늘의 추천 배합",
                            tint = Color(0xFF2E7D32)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("오늘의 추천 배합", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    SupplementRow("마그네슘", "근육 이완 & 스트레스 완화", "225mg", R.drawable.bolt)
                    SupplementRow("아연", "면역력 강화 & 상처 치유", "6mg", R.drawable.shield)
                    SupplementRow("전해질", "수분 균형 & 근육 기능", "350mg", R.drawable.humidity_low)
                    SupplementRow("멜라토닌", "수면 질 개선 & 생체리듬", "3mg", R.drawable.moon_stars)
                }
            }

            Spacer(Modifier.height(24.dp))

            // 한 잔 배출하기 버튼
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        val uuid = dispenserUuid?.trim().orEmpty()
                        if (uuid.isEmpty()) {
                            // ⛳️ onClick에서는 LaunchedEffect 쓰면 안 됨!
                            scope.launch { snackbarHostState.showSnackbar("디스펜서를 먼저 등록해 주세요.") }
                            return@Button
                        }
                        vm.requestIntake(profileId = profileId, dispenserUuid = uuid)
                    },
                    enabled = !ui.value.loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.glass_cup),
                            contentDescription = "한 잔 배출하기",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .offset(y = (-2).dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (ui.value.loading) "배출 중..." else "한 잔 배출하기",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplementRow(
    title: String,
    desc: String,
    amount: String,
    iconId: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = title,
                modifier = Modifier
                    .size(20.dp)
                    .offset(y = 5.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(desc, fontSize = 12.sp, color = Color(0xFF6F7783))
            }
        }
        Box(
            modifier = Modifier
                .width(70.dp)
                .background(Color(0xFF424242), RoundedCornerShape(6.dp))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(amount, color = Color.White, fontSize = 12.sp)
        }
    }
}