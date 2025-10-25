package com.example.healthcaredispenser.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

private val SurfaceGreen = Color(0xFFE8F5E9)
private val OutlineGray  = Color(0xFF6F7783)
private val TextPrimary  = Color(0xFF1A1A1A)
private val TextSecondary= Color(0xFF6F7783)

@Composable
fun RecordScreen(
    navController: NavHostController,
    profileId: Long
) {
    val vm: IntakeViewModel = viewModel()
    val recent by vm.recent4.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(profileId) { vm.load(profileId) }

    Scaffold(
        bottomBar = {
            BottomBar(
                currentRoute   = Routes.RECORD,
                onHomeClick    = {
                    navController.navigate("${Routes.HOME}/$profileId") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onRecordClick  = { /* 이미 기록 */ },
                onSettingsClick= {
                    navController.navigate("${Routes.SETTINGS}/$profileId") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)               // 바텀바 패딩 자동 반영
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "섭취 기록",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // ── 컨디션 기록 카드 ─
            SectionCard(
                leadingIconId = R.drawable.graph_6,
                title = "컨디션 기록",
                rightIconId = null,
                onRightClick = null
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ActionButtonWhite(
                        text = "기록하기",
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) { navController.navigate("${Routes.CONDITION_RECORD}/$profileId") }

                    ActionButtonWhite(
                        text = "자세히보기",
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) { navController.navigate("${Routes.CONDITION_HISTORY}/$profileId") }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 복용 기록 카드 ─
            SectionCard(
                leadingIconId = R.drawable.calendar_month,
                title = "복용 기록",
                rightIconId = R.drawable.more,
                onRightClick = { /* TODO: 정렬/필터 */ }
            ) {
                when {
                    loading -> Text("불러오는 중...", color = TextSecondary)
                    error != null -> Text("기록을 불러오지 못했어요", color = Color(0xFFD32F2F))
                    recent.isEmpty() -> Text("복용 기록이 없어요", color = TextSecondary)
                    else -> {
                        recent.forEach { log ->
                            IntakeRow(
                                title = vm.toUiTime(log.completedAt ?: log.requestedAt),
                                subtitle = vm.buildSummary(log),
                                onClick = { navController.navigate("${Routes.INTAKE_HISTORY}/$profileId") }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { navController.navigate("${Routes.INTAKE_HISTORY}/$profileId") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor   = Color.Black
                    ),
                    border = BorderStroke(1.dp, OutlineGray)
                ) { Text("자세히보기", fontWeight = FontWeight.SemiBold) }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SectionCard(
    leadingIconId: Int,
    title: String,
    rightIconId: Int?,
    onRightClick: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceGreen, RoundedCornerShape(16.dp))
            .border(1.dp, SolidColor(OutlineGray), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(painterResource(id = leadingIconId), contentDescription = null, tint = TextPrimary)
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.weight(1f))
            if (rightIconId != null && onRightClick != null) {
                IconButton(onClick = onRightClick) {
                    Icon(painterResource(id = rightIconId), contentDescription = "자세히 보기", tint = TextPrimary)
                }
            }
        }
        Spacer(Modifier.height(8.dp))   // 간격 정리
        content()
    }
}

@Composable
private fun ActionButtonWhite(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor   = Color.Black
        ),
        border = BorderStroke(1.dp, OutlineGray)
    ) { Text(text, fontWeight = FontWeight.Medium) }
}

@Composable
private fun IntakeRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, color = TextSecondary)
        }
        Text(">", color = TextSecondary, fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
    }
}
