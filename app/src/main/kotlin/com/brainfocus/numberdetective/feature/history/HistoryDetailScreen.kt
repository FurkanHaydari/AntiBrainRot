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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.rotate
import androidx.hilt.navigation.compose.hiltViewModel
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.feature.result.DiagnosticEngine
import com.brainfocus.numberdetective.core.designsystem.*

@Composable
fun HistoryDetailScreen(
    sessionId: String,
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val history by viewModel.history.collectAsState()
    val session = remember(history, sessionId) { history.find { it.id == sessionId } }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scaleFactor = (maxHeight.value / 720f).coerceIn(1.0f, 2.2f)
        val maxWidthDp = maxWidth

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
        
        // --- Layer 1.5: Watermark ---
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CLASSIFIED",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = (90 * scaleFactor).sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                ),
                color = Color.White.copy(alpha = 0.04f),
                modifier = Modifier.rotate(-35f)
            )
        }

        // --- Layer 2: Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            if (session == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Session not found", color = ErrorRed)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp * scaleFactor),
                    contentPadding = PaddingValues(top = 16.dp * scaleFactor, bottom = 32.dp * scaleFactor),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header (Scrollable)
                    item {
                        DetectiveHeader(
                            title = stringResource(R.string.final_report),
                            scaleFactor = scaleFactor,
                            rightContent = {
                                IconButton(
                                    onClick = onNavigateBack,
                                    modifier = Modifier
                                        .size(40.dp * scaleFactor)
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp * scaleFactor))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = PrimaryCyan,
                                        modifier = Modifier.size(20.dp * scaleFactor)
                                    )
                                }
                            }
                        )
                    }

                    // SIA Official Header Component (Scrollable)
                    item {
                        SIAOfficialHeader(scaleFactor = scaleFactor)
                    }

                    // Case ID (Scrollable)
                    item {
                        Text(
                            text = "CASE ID: #${session.id.take(8).uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = (9 * scaleFactor).sp,
                                letterSpacing = (1 * scaleFactor).sp
                            ),
                            color = TextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp * scaleFactor)
                        )
                    }

                    // Neuro-Diagnostic Report (Scrollable)
                    item {
                        val report = session.diagnosticReport ?: DiagnosticEngine.generateReport(session)
                        com.brainfocus.numberdetective.feature.result.components.CognitiveDiagnosticReport(
                            report = report,
                            isWin = session.isWin,
                            scaleFactor = scaleFactor,
                            staggered = false, // Immediate view for history
                            modifier = Modifier.padding(bottom = 8.dp * scaleFactor)
                        )
                    }

                    // Level Details and Hints
                    session.levels?.forEach { levelResult ->
                        item {
                            DetectiveHeader(
                                title = stringResource(R.string.case_file_level, levelResult.levelNumber),
                                subtitle = stringResource(R.string.score_points, levelResult.scoreGained),
                                scaleFactor = scaleFactor,
                                rightContent = {
                                    Text(
                                        text = levelResult.secretNumber,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = (18 * scaleFactor).coerceAtMost(26f).sp
                                        ),
                                        color = SuccessGreen
                                    )
                                }
                            )
                        }
                        
                        items(levelResult.hints.size) { globalIndex ->
                            val hint = levelResult.hints[globalIndex]
                            val isUserGuess = hint.descriptionRes == com.brainfocus.numberdetective.R.string.log_analysis_attempt
                            
                            val label = if (hint.descriptionRes == com.brainfocus.numberdetective.R.string.log_analysis_success) {
                                stringResource(com.brainfocus.numberdetective.R.string.log_analysis_success)
                            } else if (isUserGuess) {
                                val interrogationNumber = levelResult.hints.take(globalIndex + 1).count { 
                                    it.descriptionRes == com.brainfocus.numberdetective.R.string.log_analysis_attempt 
                                }
                                stringResource(com.brainfocus.numberdetective.R.string.log_interrogation_number, interrogationNumber)
                            } else {
                                val intelligenceNumber = levelResult.hints.take(globalIndex + 1).count { 
                                    it.descriptionRes != com.brainfocus.numberdetective.R.string.log_analysis_attempt && 
                                    it.descriptionRes != com.brainfocus.numberdetective.R.string.log_analysis_success 
                                }
                                stringResource(com.brainfocus.numberdetective.R.string.initial_intelligence_number, intelligenceNumber)
                            }

                            DetectiveHintCard(
                                hint = hint,
                                isHelperModeEnabled = true, // History detail always shows hints
                                scaleFactor = scaleFactor,
                                label = label,
                                labelColor = if (hint.descriptionRes == com.brainfocus.numberdetective.R.string.log_analysis_success) SuccessGreen 
                                             else if (isUserGuess) PrimaryCyan 
                                             else TextSecondary.copy(alpha = 0.6f),
                                isInterrogation = isUserGuess || hint.descriptionRes == com.brainfocus.numberdetective.R.string.log_analysis_success,
                                maxWidth = 550.dp * scaleFactor
                            )
                        }
                    }
                }
            }
        }
    }
}
