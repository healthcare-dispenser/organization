@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.example.healthcaredispenser.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healthcaredispenser.data.model.profile.ProfileDto
import com.example.healthcaredispenser.navigation.Routes
import com.example.healthcaredispenser.ui.profile.ProfileViewModel
import com.example.healthcaredispenser.ui.theme.LoginGreen

@Composable
fun ProfileScreen(
    navController: NavController,
    vm: ProfileViewModel = viewModel()
) {
    val ui by vm.ui.collectAsState()

    // 삭제 확인 다이얼로그용 상태
    var confirmDeleteId by remember { mutableStateOf<Long?>(null) }

    // 첫 진입 시 목록 로드
    LaunchedEffect(Unit) { vm.fetch() }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로", tint = Color.Black)
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(96.dp))

            // 제목
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = "어떤 프로필로 시작할까요?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Spacer(Modifier.height(28.dp))

            when {
                ui.error != null -> {
                    Text(
                        text = "불러오기 실패: ${ui.error}",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.fetch() }) { Text("다시 시도") }
                }
                ui.profiles.isEmpty() && !ui.saving -> {
                    Text("등록된 프로필이 없습니다.", fontSize = 16.sp, color = Color.Gray)
                }
                else -> {
                    // 자동 줄바꿈으로 프로필 아바타 나열
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                        // 필요하면 행당 최대 개수 제한: maxItemsInEachRow = 3
                    ) {
                        ui.profiles.forEach { p ->
                            ProfileAvatarItem(
                                profile = p,
                                onClick = {
                                    // ✅ 아바타 본체 터치 → QR 화면으로 이동
                                    navController.navigate(Routes.QRSCAN) // ← 프로젝트 라우트에 맞게 바꿔줘
                                },
                                onDeleteClick = { id ->
                                    confirmDeleteId = id // 다이얼로그 띄우기
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // + 버튼 (습관 선택으로 이동)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable { navController.navigate(Routes.HABITS) },
                    shape = CircleShape,
                    color = LoginGreen,
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "프로필 추가",
                            tint = Color.White
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("프로필 추가", fontSize = 14.sp, color = Color.Black)
            }

            Spacer(Modifier.height(150.dp))
        }
    }

    // ✅ 삭제 확인 다이얼로그
    confirmDeleteId?.let { id ->
        val name = ui.profiles.firstOrNull { it.id == id }?.name ?: "이름없음"
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            title = { Text("프로필 삭제") },
            text = { Text("“$name” 프로필을 정말 삭제할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.delete(id)
                        confirmDeleteId = null
                    }
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteId = null }) { Text("취소") }
            }
        )
    }
}

@Composable
private fun ProfileAvatarItem(
    profile: ProfileDto,
    onClick: () -> Unit,
    onDeleteClick: (Long) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(88.dp),
            contentAlignment = Alignment.Center
        ) {
            // 아바타 (라벨은 아래 Text로)
            Box(Modifier.matchParentSize().clickable(onClick = onClick)) {
                ProfileAvatarCanvas(
                    diameter = 88.dp,
                    stroke = 2.dp,
                    color = LoginGreen,
                    label = "",
                    onClick = onClick
                )
            }
            // 우상단 X 삭제 버튼
            IconButton(
                onClick = { profile.id?.let(onDeleteClick) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .offset(x = 2.dp, y = (-2).dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "삭제",
                    tint = LoginGreen
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(profile.name ?: "이름없음", fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
private fun ProfileAvatarCanvas(
    diameter: Dp,
    stroke: Dp,
    color: Color,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(diameter)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = stroke.toPx()
                val radius = size.minDimension / 2f - strokePx / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // 바깥 원
                drawCircle(
                    color = color,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokePx)
                )

                // 머리
                val headRadius = radius * 0.32f
                val headCenter = Offset(center.x, center.y - radius * 0.28f)
                drawCircle(
                    color = color,
                    radius = headRadius,
                    center = headCenter,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // 어깨
                val shoulderRadius = radius * 0.62f
                val y = center.y + radius * 0.7f
                drawArc(
                    color = color,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(center.x - shoulderRadius, y - shoulderRadius),
                    size = androidx.compose.ui.geometry.Size(shoulderRadius * 2, shoulderRadius * 2),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        if (label.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(label, fontSize = 14.sp, color = Color.Black)
        }
    }
}
