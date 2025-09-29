@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.healthcaredispenser.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthcaredispenser.R

@SuppressLint("UnrememberedMutableState")
@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSubmitClick: (email: String, password: String, passwordConfirm: String) -> Unit = { _, _, _ -> }
) {
    // colors
    val loginGreen = Color(0xFF2E7D32)
    val borderGray = Color(0xFFD0D5DD)

    var email            by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var passwordConfirm  by remember { mutableStateOf("") }   // ✅ confirm 하나만 사용

    var showPw           by remember { mutableStateOf(false) }
    var showPwConfirm    by remember { mutableStateOf(false) }

    var emailError       by remember { mutableStateOf<String?>(null) }
    var pwConfirmError   by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val emailFocus   = remember { FocusRequester() }
    val pwFocus      = remember { FocusRequester() }
    val pwcFocus     = remember { FocusRequester() }

    fun isEmailValid(s: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()

    fun validateAll(): Boolean {
        emailError = when {
            email.isBlank() -> "이메일을 입력하세요"
            !isEmailValid(email) -> "이메일 형식이 올바르지 않습니다"
            else -> null
        }
        pwConfirmError = when {
            passwordConfirm.isBlank() -> "비밀번호 확인을 입력하세요"
            passwordConfirm != password -> "비밀번호가 일치하지 않습니다"
            else -> null
        }
        return emailError == null && pwConfirmError == null
    }

    val isFormValid by derivedStateOf {
        email.isNotBlank() && isEmailValid(email) &&
                password.isNotBlank() &&
                passwordConfirm.isNotBlank() && passwordConfirm == password
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // back
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .shadow(8.dp, CircleShape, clip = true)
                .background(Color.White)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back_2),
                contentDescription = "뒤로가기",
                tint = Color(0xFF222222)
            )
        }

        Spacer(Modifier.height(100.dp))

        Text(
            text = "회원가입",
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 40.sp
        )

        Spacer(Modifier.height(32.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (emailError != null) emailError = null
            },
            label = { Text("이메일") },
            isError = emailError != null,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .focusRequester(emailFocus),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { pwFocus.requestFocus() }),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = loginGreen,
                unfocusedBorderColor = borderGray,
                cursorColor = loginGreen,
                focusedLabelColor = loginGreen,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(Modifier.height(20.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                pwConfirmError = if (passwordConfirm.isNotBlank() && passwordConfirm != it)
                    "비밀번호가 일치하지 않습니다" else null
            },
            label = { Text("비밀번호") },
            singleLine = true,
            visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPw = !showPw }) {
                    Icon(
                        imageVector = if (showPw) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showPw) "비밀번호 숨기기" else "비밀번호 보이기"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .focusRequester(pwFocus),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { pwcFocus.requestFocus() }),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = loginGreen,
                unfocusedBorderColor = borderGray,
                cursorColor = loginGreen,
                focusedLabelColor = loginGreen,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(Modifier.height(20.dp))

        // Password Confirm
        OutlinedTextField(
            value = passwordConfirm,                               // ✅ confirm 상태와 연결
            onValueChange = {
                passwordConfirm = it
                pwConfirmError = when {
                    it.isBlank() -> null
                    it != password -> "비밀번호가 일치하지 않습니다"
                    else -> null
                }
            },
            label = { Text("비밀번호 확인") },
            isError = pwConfirmError != null,
            singleLine = true,
            visualTransformation = if (showPwConfirm) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPwConfirm = !showPwConfirm }) {
                    Icon(
                        imageVector = if (showPwConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showPwConfirm) "비밀번호 숨기기" else "비밀번호 보이기"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .focusRequester(pwcFocus),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                if (validateAll()) onSubmitClick(email.trim(), password, passwordConfirm) // ✅ confirm 전송
            }),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = loginGreen,
                unfocusedBorderColor = borderGray,
                cursorColor = loginGreen,
                focusedLabelColor = loginGreen,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                if (validateAll()) onSubmitClick(email.trim(), password, passwordConfirm)  // ✅ confirm 전송
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = loginGreen,
                disabledContainerColor = loginGreen.copy(alpha = 0.4f)
            )
        ) {
            Text("회원가입", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSignup() {
    MaterialTheme { SignupScreen() }
}
