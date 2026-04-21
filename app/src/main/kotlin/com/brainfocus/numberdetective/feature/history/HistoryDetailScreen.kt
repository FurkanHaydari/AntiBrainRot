package com.brainfocus.numberdetective.feature.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.draw.clip
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
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Briefing, 1: Archive

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

        // --- Layer 2: Main Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (session == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Session not found", color = ErrorRed)
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp * scaleFactor))

                // Persistent Header with Back Button
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

                Spacer(modifier = Modifier.height(8.dp * scaleFactor))

                // Custom Tab Bar (Consistent with ResultScreen)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp * scaleFactor)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp * scaleFactor))
                        .padding(4.dp * scaleFactor)
                ) {
                    TabItem(
                        text = stringResource(R.string.label_tab_briefing),
                        isSelected = selectedTab == 0,
                        scaleFactor = scaleFactor,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 0 }
                    )
                    TabItem(
                        text = stringResource(R.string.label_tab_archive),
                        isSelected = selectedTab == 1,
                        scaleFactor = scaleFactor,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 1 }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp * scaleFactor))

                // Tab Content Switcher
                Crossfade(
                    targetState = selectedTab,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    label = "HistoryTabTransition"
                ) { tab ->
                    when (tab) {
                        0 -> BriefingTabContent(session, scaleFactor, maxWidthDp)
                        1 -> ArchiveTabContent(session, scaleFactor)
                    }
                }
            }
        }
    }
}

@Composable
private fun BriefingTabContent(
    session: com.brainfocus.numberdetective.data.storage.GameSession,
    scaleFactor: Float,
    maxWidth: androidx.compose.ui.unit.Dp
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val verticalScale = (maxHeight.value / 720f).coerceIn(1.0f, 2.5f)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Digital Intelligence HUD (Transparent Shell)
            Box(
                modifier = Modifier
                    .widthIn(max = (500.dp * verticalScale).coerceAtMost(this@BoxWithConstraints.maxWidth))
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(16.dp * verticalScale))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(16.dp * verticalScale)
                    )
                    .padding(horizontal = 24.dp * verticalScale, vertical = 28.dp * verticalScale)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SIAOfficialHeader(scaleFactor = verticalScale)
                    
                    Spacer(modifier = Modifier.height(12.dp * verticalScale))
                    
                    Text(
                        text = "INTELLIGENCE FEED: #${session.id.take(12).uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = (9 * verticalScale).sp,
                            letterSpacing = (2 * verticalScale).sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PrimaryCyan.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp * verticalScale))

                    val report = session.diagnosticReport ?: DiagnosticEngine.generateReport(session)
                    com.brainfocus.numberdetective.feature.result.components.CognitiveDiagnosticReport(
                        report = report,
                        isWin = session.isWin,
                        scaleFactor = verticalScale,
                        staggered = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveTabContent(
    session: com.brainfocus.numberdetective.data.storage.GameSession,
    scaleFactor: Float
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp * scaleFactor),
        contentPadding = PaddingValues(bottom = 32.dp * scaleFactor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    isHelperModeEnabled = true,
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

@Composable
private fun TabItem(
    text: String, 
    isSelected: Boolean, 
    scaleFactor: Float, 
    modifier: Modifier = Modifier, 
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp * scaleFactor))
            .background(if (isSelected) PrimaryCyan.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp * scaleFactor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = (1.5 * scaleFactor).sp,
                fontSize = (12 * scaleFactor).coerceAtMost(18f).sp
            ),
            color = if (isSelected) PrimaryCyan else TextSecondary
        )
    }
}
