package com.example.healthcaredispenser.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healthcaredispenser.ui.auth.AuthViewModel
import com.example.healthcaredispenser.ui.screens.HabitsScreen
import com.example.healthcaredispenser.ui.screens.HomeScreen
import com.example.healthcaredispenser.ui.screens.ProfileAddScreen
import com.example.healthcaredispenser.ui.screens.ProfileScreen
import com.example.healthcaredispenser.ui.screens.QRScanScreen
import com.example.healthcaredispenser.ui.screens.RecordScreen
import com.example.healthcaredispenser.ui.screens.SignupScreen
import com.example.healthcaredispenser.ui.screens.WelcomeScreen
import com.example.healthcaredispenser.ui.screens.ConditionRecordScreen

object Routes {
    const val WELCOME = "welcome"
    const val SIGNUP  = "signup"
    const val PROFILE = "profile"
    const val HABITS  = "habits"       // 프로필 만들기 1단계: 습관 선택(최소 3개)
    const val PROFILE_ADD = "profile_add" // 프로필 만들기 2단계: 기본정보 입력/저장
    const val QRSCAN  = "qrscan"

    // ⬇️ === 수정된 부분 === ⬇️
    const val HOME = "home" // 기존 BottomBar 호환용
    const val RECORD = "record" // 기존 BottomBar 호환용
    const val SETTINGS = "settings" // 기존 BottomBar 호환용
    const val CONDITION_RECORD = "condition_record"

    // profileId를 받는 실제 이동 경로
    private const val ARG_ID = "profileId"
    const val HOME_ROUTE = "$HOME/{$ARG_ID}"
    const val RECORD_ROUTE = "$RECORD/{$ARG_ID}"
    const val SETTINGS_ROUTE = "$SETTINGS/{$ARG_ID}"
    // ⬆️ =================== ⬆️
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

        // ⬇️ === 추가된 부분 === ⬇️

        // 7) 홈 화면 (profileId 인자 받음)
        composable(
            route = Routes.HOME_ROUTE, // "home/{profileId}"
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: -1L

            if (profileId == -1L) {
                // 비정상 접근, 프로필 선택으로 복귀
                navController.popBackStack(Routes.PROFILE, false)
            } else {
                HomeScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRecord = { navController.navigate("${Routes.RECORD}/$profileId") { launchSingleTop = true } },
                    onNavigateToSettings = { navController.navigate("${Routes.SETTINGS}/$profileId") { launchSingleTop = true } },
                    profileId = profileId
                )
            }
        }

        // 8) 기록 화면 (BottomBar 탐색용)
        composable(
            route = Routes.RECORD_ROUTE, // "record/{profileId}"
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) {
            // RecordScreen은 profileId를 인자로 받지 않지만, BottomBar를 위해 NavController만 넘김
            RecordScreen(navController = navController)
        }

        // 9) 설정 화면 (BottomBar 탐색용)
        composable(
            route = Routes.SETTINGS_ROUTE, // "settings/{profileId}"
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) {
            // TODO: SettingsScreen 구현 필요
            Text(text = "설정 화면 (Profile ID: ${it.arguments?.getLong("profileId")})")
        }

        // 10) 컨디션 기록화면
        composable(Routes.CONDITION_RECORD) {
            ConditionRecordScreen(navController = navController)
        }
    }
}