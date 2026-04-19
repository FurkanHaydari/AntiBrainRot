package com.brainfocus.numberdetective.feature

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.brainfocus.numberdetective.feature.home.HomeScreen
import com.brainfocus.numberdetective.feature.game.GameScreen
import com.brainfocus.numberdetective.feature.result.ResultScreen
import com.brainfocus.numberdetective.feature.game.GameViewModel
import com.brainfocus.numberdetective.feature.onboarding.OnboardingScreen
import com.brainfocus.numberdetective.feature.onboarding.OnboardingViewModel
import com.brainfocus.numberdetective.feature.history.HistoryScreen
import com.brainfocus.numberdetective.feature.history.HistoryDetailScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import com.brainfocus.numberdetective.core.designsystem.SurfaceCard

@Composable
fun AppNavigation(
    onPlayClick: () -> Unit,
    onLanguageChange: (String) -> Unit,
    currentLanguage: String
) {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val isFirstLaunch by onboardingViewModel.isFirstLaunch.collectAsState()

    if (isFirstLaunch == null) {
        Box(modifier = androidx.compose.ui.Modifier.fillMaxSize().background(SurfaceCard))
        return
    }

    // Determine starting point based on persistence
    val startDest = if (isFirstLaunch == true) "onboarding" else "home"

    NavHost(navController = navController, startDestination = startDest) {
        composable("onboarding") {
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onFinish = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onPlayClick = {
                    onPlayClick()
                    navController.navigate("game")
                },
                onManualClick = {
                    navController.navigate("onboarding")
                },
                onHistoryClick = {
                    navController.navigate("history")
                },
                onLanguageChange = onLanguageChange,
                currentLanguage = currentLanguage
            )
        }
        composable("history") {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { sessionId ->
                    navController.navigate("history_detail/$sessionId")
                }
            )
        }
        composable("history_detail/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            HistoryDetailScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("game") {
            val gameViewModel: GameViewModel = hiltViewModel()
            GameScreen(
                viewModel = gameViewModel,
                onNavigateToResult = { isWin, score, correctAnswer, attempts, timeInSeconds, dailyHighScore, allTimeHighScore -> 
                    navController.navigate("result/$isWin/$score/$correctAnswer/$attempts/$timeInSeconds/$dailyHighScore/$allTimeHighScore") {
                        popUpTo("home")
                    }
                },
                onNavigateBack = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
        composable("result/{isWin}/{score}/{correctAnswer}/{attempts}/{timeInSeconds}/{dailyHighScore}/{allTimeHighScore}") { backStackEntry ->
            val isWin = backStackEntry.arguments?.getString("isWin")?.toBoolean() ?: false
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val correctAnswer = backStackEntry.arguments?.getString("correctAnswer") ?: ""
            val attempts = backStackEntry.arguments?.getString("attempts")?.toIntOrNull() ?: 0
            val timeInSeconds = backStackEntry.arguments?.getString("timeInSeconds")?.toIntOrNull() ?: 0
            val dailyHighScore = backStackEntry.arguments?.getString("dailyHighScore")?.toIntOrNull() ?: 0
            val allTimeHighScore = backStackEntry.arguments?.getString("allTimeHighScore")?.toIntOrNull() ?: 0

            ResultScreen(
                isWin = isWin,
                score = score,
                correctAnswer = correctAnswer,
                attempts = attempts,
                timeInSeconds = timeInSeconds,
                dailyHighScore = dailyHighScore,
                allTimeHighScore = allTimeHighScore,
                onPlayAgain = {
                    navController.navigate("game") { popUpTo("home") }
                },
                onGoHome = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
    }
}
