package com.example.healthcaredispenser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthcaredispenser.R
import com.example.healthcaredispenser.navigation.Routes

@Composable
fun BottomBar(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onRecordClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val bg        = Color(0xFFE8F5E9) // 바 배경
    val topLine   = Color(0xFFD9D9D9) // 상단 라인
    val active    = Color(0xFF2E7D32) // 선택 색
    val inactive  = Color(0xFF000000) // 비선택 색

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
    ) {
        // 상단 테두리 라인만
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(topLine)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                title = "홈",
                iconRes = R.drawable.home,
                selected = currentRoute == Routes.HOME,
                activeColor = active,
                inactiveColor = inactive,
                onClick = onHomeClick
            )
            BottomBarItem(
                title = "기록",
                iconRes = R.drawable.insert_chart,
                selected = currentRoute == Routes.RECORD,
                activeColor = active,
                inactiveColor = inactive,
                onClick = onRecordClick
            )
            BottomBarItem(
                title = "설정",
                iconRes = R.drawable.settings,
                selected = currentRoute == Routes.SETTINGS,
                activeColor = active,
                inactiveColor = inactive,
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    title: String,
    iconRes: Int,
    selected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val tint = if (selected) activeColor else inactiveColor
    // ✅ ripple 제거: clickable(indication = null, interactionSource = remember { MutableInteractionSource() })
    val noRipple = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable(
                interactionSource = noRipple,
                indication = null, // <- 회색 눌림 표시 완전 제거
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = title,
            color = tint,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 14.sp
        )
    }
}