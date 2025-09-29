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

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onSignUpClick: () -> Unit = {}
) {
    // Colors by spec
    val borderGray = Color(0xFFD0D5DD) // 이메일/비밀번호 박스 테두리
    val loginGreen = Color(0xFF2E7D32) // 로그인 버튼
    val signBg = Color(0xFFE8F5E9)     // 회원가입 박스 배경
    val hintGray = Color(0xFF6F7783)   // 플레이스홀더

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

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("이메일 입력") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { pwFocusRequester.requestFocus() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderGray,
                unfocusedBorderColor = borderGray,
                cursorColor = loginGreen,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedPlaceholderColor = hintGray,
                unfocusedPlaceholderColor = hintGray
            )
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .focusRequester(pwFocusRequester),
            placeholder = { Text("비밀번호 입력") },
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderGray,
                unfocusedBorderColor = borderGray,
                cursorColor = loginGreen,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedPlaceholderColor = hintGray,
                unfocusedPlaceholderColor = hintGray
            )
        )

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
            colors = ButtonDefaults.buttonColors(containerColor = loginGreen)
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
                containerColor = signBg,
                contentColor = loginGreen
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
