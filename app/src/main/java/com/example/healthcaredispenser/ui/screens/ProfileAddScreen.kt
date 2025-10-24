@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.healthcaredispenser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healthcaredispenser.data.model.profile.CreateProfileRequest
import com.example.healthcaredispenser.navigation.Routes
import com.example.healthcaredispenser.ui.profile.ProfileViewModel
import com.example.healthcaredispenser.ui.theme.BorderGray
import com.example.healthcaredispenser.ui.theme.HintGray
import com.example.healthcaredispenser.ui.theme.LoginGreen
import com.example.healthcaredispenser.ui.theme.SignBg

// UI 토큰
private object AddUI {
    val ScreenSide = 20.dp
    val CardRadius = 12.dp
    val CardPad = 14.dp
    val FieldGap = 10.dp
    val SectionGap = 16.dp
    val TitleTop = 8.dp
    val FieldCorner = 10.dp
    val BtnHeight = 48.dp
}

@Composable
fun ProfileAddScreen(
    navController: NavController,
    profileId: Long = -1L, // ⭐️ 1. profileId 인자 추가 (기본값 -1L)
    vm: ProfileViewModel = viewModel()
) {
    // ⭐️ 2. "수정 모드"인지 확인
    val isEditMode = profileId != -1L

    // 입력값
    var name by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }   // 서버 전송 X, UI만
    var height by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }

    var genderExpanded by rememberSaveable { mutableStateOf(false) }
    var gender by rememberSaveable { mutableStateOf("남성") }

    var isPregnant by rememberSaveable { mutableStateOf(false) }
    var hasLiver by rememberSaveable { mutableStateOf(false) }
    var hasKidney by rememberSaveable { mutableStateOf(false) }
    var hasCardio by rememberSaveable { mutableStateOf(false) }

    // ✅ HabitsScreen에서 전달된 습관 코드 수신
    val tags = remember { mutableStateListOf<String>() }
    LaunchedEffect(Unit) {
        val codes = navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<ArrayList<String>>("chosenHabits")
            ?: arrayListOf()
        tags.clear()
        tags.addAll(codes.distinct())
        // 재입장 시 중복 방지
        navController.previousBackStackEntry?.savedStateHandle?.set("chosenHabits", null)
    }

    // ⭐️ 3. 수정 모드일 때, 기존 데이터 불러오기
    LaunchedEffect(profileId) {
        if (isEditMode) {
            // ViewModel에 캐시된 profiles 리스트에서 원본 데이터 찾기
            val profileToEdit = vm.getProfileById(profileId)
            if (profileToEdit != null) {
                name = profileToEdit.name ?: ""
                height = profileToEdit.height?.toString() ?: ""
                weight = profileToEdit.weight?.toString() ?: ""
                gender = if (profileToEdit.gender == "FEMALE") "여성" else "남성"

                // ⭐️ 주의: 'age'는 Dto에 없으므로 불러올 수 없음

                // ⭐️ Habits(tags)는 HabitsScreen에서 새로 받아오므로 여기서 덮어쓰지 않음
                // ⭐️ (만약 Habits도 기존 값을 불러와야 한다면 로직 추가 필요)

                // ⭐️ Conditions(특이사항) 불러오기
                profileToEdit.conditions?.let { conds ->
                    isPregnant = conds.contains("PREGNANT")
                    hasLiver = conds.contains("LIVER_DISEASE")
                    hasKidney = conds.contains("KIDNEY_DISEASE")
                    hasCardio = conds.contains("CARDIOVASCULAR")
                }
            }
        }
    }


    val ui by vm.ui.collectAsState()

    // 숫자만 입력 허용
    fun onlyDigits(old: String, new: String) =
        if (new.all { it.isDigit() } || new.isBlank()) new else old

    val heightNum = height.toDoubleOrNull()
    val weightNum = weight.toDoubleOrNull()
    val validNumbers = heightNum != null && weightNum != null
    val validRequired = name.isNotBlank() && height.isNotBlank() && weight.isNotBlank()
    val validTags = tags.size >= 3
    val canSave = validRequired && validNumbers && validTags && !ui.saving

    // 저장 성공 → PROFILE로 복귀 (생성/수정 모두 동일하게 동작)
    LaunchedEffect(ui.saved) {
        if (ui.saved) {
            vm.clearSavedFlag()
            // ⭐️ 수정이든 생성이든 완료되면 '프로필 목록'으로 돌아감
            navController.popBackStack(Routes.PROFILE, inclusive = false)
        }
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { BackBar(onBack = { navController.popBackStack() }) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AddUI.ScreenSide, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                    modifier = Modifier
                        .height(AddUI.BtnHeight)
                        .weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("이전") }

                Button(
                    onClick = {
                        // ⭐️ 4. 요청 객체 생성 (동일)
                        val req = CreateProfileRequest(
                            name = name,
                            height = heightNum ?: 0.0,
                            weight = weightNum ?: 0.0,
                            gender = if (gender == "남성") "MALE" else "FEMALE",
                            tags = tags.toList(), // ✅ 선택 습관 전송
                            conditions = buildList {
                                if (isPregnant) add("PREGNANT")
                                if (hasLiver) add("LIVER_DISEASE")       // ✅ 서버 enum
                                if (hasKidney) add("KIDNEY_DISEASE")     // ✅ 서버 enum
                                if (hasCardio) add("CARDIOVASCULAR")     // ✅ 서버 enum
                            }
                        )

                        // ⭐️ 5. 수정 모드/생성 모드 분기
                        if (isEditMode) {
                            vm.update(profileId, req) // 👈 수정
                        } else {
                            vm.create(req) // 👈 생성
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .height(AddUI.BtnHeight)
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LoginGreen,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFBFBFBF),
                        disabledContentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (ui.saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("저장")
                    }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = AddUI.ScreenSide)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(AddUI.TitleTop))

            // 선택한 습관 미리보기
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("선택한 습관: ${tags.size}개", fontSize = 13.sp, color = Color.Black)
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("다시 선택", color = LoginGreen)
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AddUI.CardRadius))
                    .background(SignBg)
                    .border(1.dp, BorderGray, RoundedCornerShape(AddUI.CardRadius))
                    .padding(AddUI.CardPad)
            ) {
                SectionHeader(
                    icon = Icons.Outlined.Person,
                    title = "개인 프로필",
                    subtitle = "맞춤형 건강관리를 위한 개인정보를 입력해주세요."
                )

                Spacer(Modifier.height(AddUI.SectionGap))

                // 이름 / 나이
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabeledField(
                        label = "이름",
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "홍길동",
                        modifier = Modifier.weight(1f),
                        corner = AddUI.FieldCorner
                    )
                    LabeledField(
                        label = "나이",
                        value = age,
                        onValueChange = { age = onlyDigits(age, it) },
                        placeholder = "21",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        corner = AddUI.FieldCorner
                    )
                }

                Spacer(Modifier.height(AddUI.FieldGap))

                // 키 / 체중
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabeledField(
                        label = "키 (cm)",
                        value = height,
                        onValueChange = { height = onlyDigits(height, it) },
                        placeholder = "170",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        corner = AddUI.FieldCorner
                    )
                    LabeledField(
                        label = "체중 (kg)",
                        value = weight,
                        onValueChange = { weight = onlyDigits(weight, it) },
                        placeholder = "65",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        corner = AddUI.FieldCorner
                    )
                }

                Spacer(Modifier.height(AddUI.FieldGap))

                // 성별 드롭다운
                Text("성별", fontSize = 13.sp, color = Color.Black)
                Spacer(Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = !genderExpanded }
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(AddUI.FieldCorner),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LoginGreen,
                            unfocusedBorderColor = BorderGray,
                            cursorColor = LoginGreen
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("남성") },
                            onClick = { gender = "남성"; genderExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("여성") },
                            onClick = { gender = "여성"; genderExpanded = false }
                        )
                    }
                }

                Spacer(Modifier.height(AddUI.SectionGap))

                SectionHeader(
                    icon = Icons.Outlined.Info,
                    title = "특이사항",
                    subtitle = "해당하는 항목에 체크해주세요."
                )

                Spacer(Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CheckRow("임산부", isPregnant) { isPregnant = it }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CheckRow("간질환", hasLiver) { hasLiver = it }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CheckRow("신장질환", hasKidney) { hasKidney = it }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CheckRow("심혈관질환", hasCardio) { hasCardio = it }
                        }
                    }
                }
            }
        }
    }
}

/* 섹션 헤더 */
@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, tint = LoginGreen)
        Column {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = HintGray)
        }
    }
}

/* 입력 필드 */
@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    corner: Dp = 10.dp
) {
    Column(modifier) {
        Text(label, fontSize = 13.sp, color = Color.Black)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = { Text(placeholder, color = HintGray) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(corner),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoginGreen,
                unfocusedBorderColor = BorderGray,
                cursorColor = LoginGreen
            )
        )
    }
}

/* 체크박스 */
@Composable
private fun CheckRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = LoginGreen,
                uncheckedColor = BorderGray
            )
        )
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 15.sp)
    }
}

/* Back 버튼 */
@Composable
private fun BackBar(
    onBack: () -> Unit,
    startPadding: Dp = 16.dp,
    topPadding: Dp = 16.dp,
    endPadding: Dp = 16.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = startPadding, top = topPadding, end = endPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로", tint = Color.Black)
        }
    }
}