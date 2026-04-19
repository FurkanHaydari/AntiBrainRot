package com.brainfocus.numberdetective.feature.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import com.brainfocus.numberdetective.feature.result.ArchiveHintCard
import com.brainfocus.numberdetective.feature.result.LevelHeader

@Composable
fun HistoryDetailScreen(
    sessionId: String,
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val history by viewModel.history.collectAsState()
    val session = remember(history, sessionId) { history.find { it.id == sessionId } }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Layer 1: Background ---
        Image(
            painter = painterResource(id = R.drawable.detective_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Black.copy(alpha = 0.98f))
                    )
                )
        )

        // --- Layer 2: Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryCyan
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.final_report).uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = Montserrat,
                        letterSpacing = 2.sp
                    ),
                    color = PrimaryCyan
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (session == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Session not found", color = ErrorRed)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    session.levels.forEach { levelResult ->
                        item {
                            LevelHeader(levelResult)
                        }
                        
                        items(levelResult.hints.size) { globalIndex ->
                            val hint = levelResult.hints[globalIndex]
                            val isUserGuess = hint.descriptionRes == R.string.log_analysis_attempt || hint.descriptionRes == R.string.log_analysis_success
                            val analysisNumber = if (isUserGuess) {
                                levelResult.hints.take(globalIndex + 1).count { it.descriptionRes == R.string.log_analysis_attempt || it.descriptionRes == R.string.log_analysis_success }
                            } else {
                                0
                            }
                            ArchiveHintCard(hint = hint, analysisNumber = analysisNumber)
                        }
                    }
                }
            }
        }
    }
}
