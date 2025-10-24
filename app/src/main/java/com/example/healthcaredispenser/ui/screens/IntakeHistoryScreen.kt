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
import com.example.healthcaredispenser.data.model.intake.IntakeItem
import com.example.healthcaredispenser.navigation.Routes
import com.example.healthcaredispenser.ui.components.BottomBar

private val SurfaceGreen = Color(0xFFE8F5E9)
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
    // 서버 연동 전: 더미 목록(비우면 빈 상태 화면)
    var items by remember { mutableStateOf(dummyIntakeItems()) }

    Scaffold(
        bottomBar = {
            BottomBar(
                currentRoute = Routes.RECORD, // '기록' 탭 강조
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
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .offset(y = 20.dp)
        ) {
            // 상단 바: 뒤로가기 + 타이틀 (arrow_back_2 사용)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back_2),
                        contentDescription = "뒤로",
                        tint = TextPrimary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "복용 기록",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 16.dp) // 기본 위치
                        .offset(x = (-55).dp, y = (40).dp) // ← X, Y로 미세조정
                )
                Spacer(Modifier.height(8.dp))
            }

            if (items.isEmpty()) {
                EmptyIntakeView(
                    onGoBack = { navController.popBackStack() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp)
                        .offset(y = 35.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.intakeId }) { log ->
                        IntakeHistoryItem(
                            timeText = (log.completedAt ?: log.requestedAt ?: ""),
                            summary  = buildSummary(log),
                            onClick  = { /* 상세 필요시 연결 */ }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

/** 카드 1행 */
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

/** 빈 상태뷰 */
@Composable
private fun EmptyIntakeView(
    onGoBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        ) {
            Text("돌아가기")
        }
        Spacer(Modifier.height(24.dp))
        Divider(color = OutlineGray.copy(alpha = 0.3f))
    }
}

/** 더미 데이터 (서버 붙이기 전) */
private fun dummyIntakeItems(): List<IntakeItem> = listOf(
    IntakeItem(
        intakeId = 1, melatonin = 3.0, status = "SUCCESS",
        requestedAt = "25/09/15 19:59", completedAt = "25/09/15 20:00"
    ),
    IntakeItem(
        intakeId = 2, magnesium = 225.0, electrolyte = 40.0, status = "SUCCESS",
        requestedAt = "25/09/15 13:59", completedAt = "25/09/15 14:00"
    ),
    IntakeItem(
        intakeId = 3, melatonin = 3.0, status = "SUCCESS",
        requestedAt = "25/09/14 19:59", completedAt = "25/09/14 20:00"
    ),
    IntakeItem(
        intakeId = 4, vitamin = 6.0, status = "SUCCESS", // 예: 아연 6mg 대신 vitamin 필드 사용 (샘플)
        requestedAt = "25/09/14 08:59", completedAt = "25/09/14 09:00"
    )
)

/** 항목 요약 문자열 생성 (null인 성분은 생략) */
private fun buildSummary(item: IntakeItem): String {
    val parts = mutableListOf<String>()
    item.melatonin?.let { parts += "멜라토닌 ${trimZero(it)}mg" }
    item.magnesium?.let { parts += "마그네슘 ${trimZero(it)}mg" }
    item.electrolyte?.let { parts += "전해질 ${trimZero(it)}mg" }
    item.vitamin?.let { parts += "비타민 ${trimZero(it)}mg" } // 필요 시 '아연' 등으로 변경
    return parts.joinToString(" , ")
}
private fun trimZero(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
