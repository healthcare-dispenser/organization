package com.example.healthcaredispenser.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.healthcaredispenser.R
import com.example.healthcaredispenser.navigation.Routes
import com.example.healthcaredispenser.ui.components.BottomBar

private val SurfaceGreen = Color(0xFFE8F5E9)      // 카드 배경
private val OutlineGray  = Color(0xFF6F7783)      // 모든 테두리
private val TextPrimary  = Color(0xFF1A1A1A)
private val TextSecondary= Color(0xFF6F7783)

@Composable
fun RecordScreen(navController: NavHostController) {
    Scaffold(
        // 홈 화면과 동일: topBar 없이 본문에 헤더 배치
        bottomBar = {
            BottomBar(
                currentRoute   = Routes.RECORD,
                onHomeClick    = { navController.navigate(Routes.HOME) { launchSingleTop = true } },
                onRecordClick  = { /* 이미 기록 */ },
                onSettingsClick= { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .offset(y = 50.dp)  // 화면 전체를 살짝 아래로
        ) {

            Spacer(Modifier.height(8.dp))

            // 제목: 뒤로가기 바로 아래
            Text(
                text = "섭취 기록",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))
            // ── 컨디션 기록 카드 ───────────────────────────────────────────────
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
                    // 흰 배경 + 검정 글씨 + 6F7783 테두리
                    ActionButtonWhite(
                        text = "기록하기",
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) { /* TODO */ }

                    ActionButtonWhite(
                        text = "자세히보기",
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) { /* TODO */ }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 복용 기록 카드 ────────────────────────────────────────────────
            SectionCard(
                leadingIconId = R.drawable.calendar_month,
                title = "복용 기록",
                rightIconId = R.drawable.more,
                onRightClick = { /* TODO: 정렬/필터 */ }
            ) {
                IntakeRow("25/09/15 20:00", "멜라토닌 3mg") { }
                IntakeRow("25/09/15 14:00", "마그네슘 225mg , 전해질 40mg") { }
                IntakeRow("25/09/14 20:00", "멜라토닌 3mg") { }
                IntakeRow("25/09/14 09:00", "아연 6mg") { }

                Spacer(Modifier.height(8.dp))

                // 하단 버튼도 동일 스타일
                OutlinedButton(
                    onClick = { /* 전체 기록 이동 */ },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor   = Color.Black
                    ),
                    border = BorderStroke(1.dp, OutlineGray)
                ) {
                    Text("자세히보기", fontWeight = FontWeight.SemiBold)
                }
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
            .border(
                width = 1.dp,
                brush = SolidColor(OutlineGray),
                shape = RoundedCornerShape(16.dp)
            )
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
        Spacer(Modifier.height(12.dp))
        content()
    }
}

// 흰 배경/검정 텍스트/6F7783 테두리 버튼
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