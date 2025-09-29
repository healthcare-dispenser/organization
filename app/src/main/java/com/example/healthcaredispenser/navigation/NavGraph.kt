package com.example.healthcaredispenser.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthcaredispenser.ui.auth.AuthViewModel
import com.example.healthcaredispenser.ui.screens.HabitsScreen
import com.example.healthcaredispenser.ui.screens.ProfileAddScreen
import com.example.healthcaredispenser.ui.screens.ProfileScreen
import com.example.healthcaredispenser.ui.screens.QRScanScreen
import com.example.healthcaredispenser.ui.screens.SignupScreen
import com.example.healthcaredispenser.ui.screens.WelcomeScreen

object Routes {
    const val WELCOME = "welcome"
    const val SIGNUP  = "signup"
    const val PROFILE = "profile"
    const val HABITS  = "habits"       // 프로필 만들기 1단계: 습관 선택(최소 3개)
    const val PROFILE_ADD = "profile_add" // 프로필 만들기 2단계: 기본정보 입력/저장
    const val QRSCAN  = "qrscan"
    const val HOME = "home"
    const val RECORD = "record"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // 인증 상태만 NavGraph 최상단에서 관찰 (로그인 성공 → PROFILE로 이동)
    val authVm: AuthViewModel = viewModel()
    val authUi = authVm.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        // 1) 웰컴 (로그인)
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onLoginClick = { email, pw -> authVm.login(email, pw) },
                onSignUpClick = { navController.navigate(Routes.SIGNUP) }
            )

            // 로그인 성공 → 프로필 목록으로
            LaunchedEffect(authUi.value.loggedIn) {
                if (authUi.value.loggedIn) {
                    navController.navigate(Routes.PROFILE) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }

        // 2) 회원가입 (성공 시 웰컴으로 돌아가서 로그인)
        composable(Routes.SIGNUP) {
            SignupScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { _, _, _ ->
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 3) 프로필 목록
        //    [+ 버튼] → Routes.HABITS 로 가도록 ProfileScreen 안에서 nav 호출
        composable(Routes.PROFILE) {
            ProfileScreen(navController = navController)
        }

        // 4) 습관 선택 화면
        //    - 최소 3개 선택 시: navController.currentBackStackEntry?.savedStateHandle?.set("selectedHabits", list)
        //    - 그리고 navController.navigate(Routes.PROFILE_ADD)
        composable(Routes.HABITS) {
            HabitsScreen(navController = navController)
        }

        // 5) 프로필 추가 화면
        //    - savedStateHandle 에서 "selectedHabits" 읽어서 CreateProfileRequest의 tags/conditions로 매핑
        //    - 저장 성공 시 popBackStack()으로 PROFILE로 복귀 (ProfileAddScreen 내부에서 처리)
        composable(Routes.PROFILE_ADD) {
            ProfileAddScreen(navController = navController)
        }

        // 6) (선택) QR 스캔
        composable(Routes.QRSCAN) {
            QRScanScreen(
                onCancel = { navController.popBackStack() },
                onSave   = { navController.popBackStack() }
            )
        }
    }
}
