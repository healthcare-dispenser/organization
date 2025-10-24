@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.healthcaredispenser.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// ⭐️ SignupScreen과 동일한 색상 사용을 위해 추가
import com.example.healthcaredispenser.ui.theme.LoginGreen
import com.example.healthcaredispenser.ui.theme.BorderGray
import com.example.healthcaredispenser.ui.theme.HintGray // 필요 시 사용 (현재는 미사용)
import com.example.healthcaredispenser.ui.theme.SignBg


@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onSignUpClick: () -> Unit = {}
) {
    // Colors (SignupScreen과 통일)
    // val borderGray = Color(0xFFD0D5DD) // BorderGray 테마 색상 사용
    // val loginGreen = Color(0xFF2E7D32) // LoginGreen 테마 색상 사용
    // val signBg = Color(0xFFE8F5E9)     // SignBg 테마 색상 사용
    // val hintGray = Color(0xFF6F7783)   // HintGray 테마 색상 사용 (필요 시)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val pwFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(120.dp))
        Text(
            text = "Healthcare\nDispenser",
            textAlign = TextAlign.Center,
            lineHeight = 44.sp,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(56.dp))

        // ⬇️ === 이메일 OutlinedTextField 수정 === ⬇️
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp), // height 고정 대신 heightIn 사용
            label = { Text("이메일") }, // placeholder -> label
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { pwFocusRequester.requestFocus() }
            ),
            colors = OutlinedTextFieldDefaults.colors( // 색상 SignupScreen과 동일하게 설정
                focusedBorderColor = LoginGreen,
                unfocusedBorderColor = BorderGray,
                cursorColor = LoginGreen,
                focusedLabelColor = LoginGreen,
                unfocusedLabelColor = Color.Gray // 비활성 레이블 색상
                // focusedTextColor, unfocusedTextColor 등은 기본값 사용
            )
        )
        // ⬆️ =================================== ⬆️

        Spacer(Modifier.height(20.dp))

        // ⬇️ === 비밀번호 OutlinedTextField 수정 === ⬇️
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp) // height 고정 대신 heightIn 사용
                .focusRequester(pwFocusRequester),
            label = { Text("비밀번호") }, // placeholder -> label
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onLoginClick(email, password)
                }
            ),
            colors = OutlinedTextFieldDefaults.colors( // 색상 SignupScreen과 동일하게 설정
                focusedBorderColor = LoginGreen,
                unfocusedBorderColor = BorderGray,
                cursorColor = LoginGreen,
                focusedLabelColor = LoginGreen,
                unfocusedLabelColor = Color.Gray // 비활성 레이블 색상
            )
        )
        // ⬆️ ===================================== ⬆️

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onLoginClick(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LoginGreen)
        ) {
            Text("로그인", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onSignUpClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SignBg, // 테마 색상 사용
                contentColor = LoginGreen // 테마 색상 사용
            )
        ) {
            Text("회원가입", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomePreview() {
    MaterialTheme { WelcomeScreen() }
}