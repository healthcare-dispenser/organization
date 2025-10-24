package com.example.healthcaredispenser.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalLifecycleOwner // ⭐️ 추가
import androidx.lifecycle.Lifecycle // ⭐️ 추가
import androidx.lifecycle.flowWithLifecycle // ⭐️ 추가
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healthcaredispenser.ui.auth.AuthNavigationEvent
import com.example.healthcaredispenser.ui.auth.AuthViewModel
import com.example.healthcaredispenser.ui.screens.ConditionRecordScreen
import com.example.healthcaredispenser.ui.screens.HabitsScreen
import com.example.healthcaredispenser.ui.screens.HomeScreen
import com.example.healthcaredispenser.ui.screens.ProfileAddScreen
import com.example.healthcaredispenser.ui.screens.ProfileScreen
import com.example.healthcaredispenser.ui.screens.QRScanScreen
import com.example.healthcaredispenser.ui.screens.RecordScreen
import com.example.healthcaredispenser.ui.screens.SettingsScreen
import com.example.healthcaredispenser.ui.screens.SignupScreen
import com.example.healthcaredispenser.ui.screens.WelcomeScreen
import com.example.healthcaredispenser.ui.screens.ConditionHistoryScreen
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.util.Log


object Routes {
    const val WELCOME = "welcome"
    const val SIGNUP  = "signup"
    const val PROFILE = "profile"
    const val QRSCAN  = "qrscan"

    // ⬇️ === 수정된 부분 === ⬇️
    private const val ARG_ID = "profileId"

    // ⭐️ "habits?profileId={profileId}" (옵션 파라미터)
    // ⭐️ (NavGraph.kt 파일 안에서 Routes.HABITS_ROUTE를 사용하도록 통일)
    const val HABITS  = "habits"
    const val HABITS_ROUTE = "$HABITS?$ARG_ID={$ARG_ID}"

    // ⭐️ "profile_add?profileId={profileId}" (옵션 파라미터)
    // ⭐️ (NavGraph.kt 파일 안에서 Routes.PROFILE_ADD_ROUTE를 사용하도록 통일)
    const val PROFILE_ADD = "profile_add"
    const val PROFILE_ADD_ROUTE = "$PROFILE_ADD?$ARG_ID={$ARG_ID}"

    // ⭐️ (기존 수정사항)
    const val HOME = "home"
    const val HOME_ROUTE = "$HOME/{$ARG_ID}"
    const val RECORD = "record"
    const val RECORD_ROUTE = "$RECORD/{$ARG_ID}"
    const val SETTINGS = "settings"
    const val SETTINGS_ROUTE = "$SETTINGS/{$ARG_ID}"
    const val CONDITION_RECORD = "condition_record"
    const val CONDITION_RECORD_ROUTE = "$CONDITION_RECORD/{$ARG_ID}"
    const val CONDITION_HISTORY = "condition_history"
    const val CONDITION_HISTORY_ROUTE = "$CONDITION_HISTORY/{$ARG_ID}"
    // ⬆️ =================== ⬆️
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // 인증 상태만 NavGraph 최상단에서 관찰 (로그인 성공 → PROFILE로 이동)
    val authVm: AuthViewModel = viewModel()
    //val authUi = authVm.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        // 1) 웰컴 (로그인)
        composable(Routes.WELCOME) {
            // ⭐️ 이전 로그인 상태를 기억하기 위한 변수
            //var previousLoggedIn by remember { mutableStateOf(authUi.value.loggedIn) }
            val lifecycleOwner = LocalLifecycleOwner.current // Lifecycle 가져오기

            // ViewModel의 Navigation Event 구독
            LaunchedEffect(authVm.navigationEvent, lifecycleOwner.lifecycle) {
                // Lifecycle 상태를 고려하여 Flow 구독 (화면 활성 시에만 이벤트 처리)
                authVm.navigationEvent.flowWithLifecycle(
                    lifecycleOwner.lifecycle,
                    Lifecycle.State.STARTED // 화면이 STARTED 상태 이상일 때만 처리
                ).collect { event ->
                    Log.d("NavGraphWelcome", "Navigation event received: $event")
                    when (event) {
                        is AuthNavigationEvent.NavigateToProfile -> {
                            Log.d("NavGraphWelcome", "Navigating to PROFILE based on event...")
                            navController.navigate(Routes.PROFILE) {
                                popUpTo(Routes.WELCOME) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        // 다른 이벤트가 있다면 여기에 추가
                    }
                }
            }
            WelcomeScreen(
                onLoginClick = { email, pw -> authVm.login(email, pw) },
                onSignUpClick = { navController.navigate(Routes.SIGNUP) }
            )


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

        // 4) ⭐️ 습관 선택 화면 (수정 모드 지원)
        composable(
            route = Routes.HABITS_ROUTE, // "habits?profileId={profileId}"
            arguments = listOf(
                navArgument("profileId") {
                    type = NavType.LongType
                    defaultValue = -1L // -1L 이면 '생성 모드'
                }
            )
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: -1L
            HabitsScreen(
                navController = navController,
                profileId = profileId // ⭐️ profileId 전달
            )
        }

        // 5) ⭐️ 프로필 추가 화면 (수정 모드 지원)
        composable(
            route = Routes.PROFILE_ADD_ROUTE, // "profile_add?profileId={profileId}"
            arguments = listOf(
                navArgument("profileId") {
                    type = NavType.LongType
                    defaultValue = -1L // -1L 이면 '생성 모드'
                }
            )
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: -1L
            ProfileAddScreen(
                navController = navController,
                profileId = profileId // ⭐️ profileId 전달
            )
        }

        // 6) QR 스캔 (기존 코드)
        composable(Routes.QRSCAN) {
            QRScanScreen(
                onCancel = { navController.popBackStack() },
                onSave   = { navController.popBackStack() }
            )
        }

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

        // 8) ⭐️ 기록 화면 (profileId 전달)
        composable(
            route = Routes.RECORD_ROUTE,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: -1L
            if (profileId == -1L) {
                LaunchedEffect(Unit) { navController.popBackStack(Routes.PROFILE, false) }
            } else {
                RecordScreen(navController = navController, profileId = profileId) // 👈 profileId 전달
            }
        }

        // 9) 설정 화면 (BottomBar 탐색용)
        composable(
            route = Routes.SETTINGS_ROUTE, // "settings/{profileId}"
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: -1L
            if (profileId == -1L) {
                // 비정상 접근, 프로필 선택으로 복귀
                navController.popBackStack(Routes.PROFILE, false)
            } else {
                SettingsScreen(
                    navController = navController,
                    profileId = profileId
                )
            }
        }

        // 10) ⭐️ 컨디션 기록 화면 (profileId 전달)
        composable(
            route = Routes.CONDITION_RECORD_ROUTE, // 👈 경로 수정
            arguments = listOf(navArgument("profileId") { type = NavType.LongType }) // 👈 인자 추가
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: -1L // 👈 profileId 받기
            if (profileId == -1L) {
                // 비정상 접근 시 이전 화면 (RecordScreen)으로
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                ConditionRecordScreen(navController = navController, profileId = profileId) // 👈 profileId 전달
            }
        }

        composable(
            route = Routes.CONDITION_HISTORY_ROUTE, // "condition_history/{profileId}"
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: -1L
            if (profileId == -1L) {
                // 비정상 접근 시 이전 화면 (RecordScreen)으로
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                ConditionHistoryScreen(navController = navController, profileId = profileId)
            }
        }
    }
}