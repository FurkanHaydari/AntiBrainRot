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
                color = Color.White.copy(alpha = 0.04f), // Slightly more visible on dark bg
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
            Spacer(modifier = Modifier.height(24.dp))
            
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp * scaleFactor)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp * scaleFactor))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryCyan,
                        modifier = Modifier.size(24.dp * scaleFactor)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp * scaleFactor))
                Text(
                    text = stringResource(R.string.final_report).uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = Montserrat,
                        letterSpacing = (2 * scaleFactor).sp,
                        fontSize = (24 * scaleFactor).coerceAtMost(32f).sp
                    ),
                    color = PrimaryCyan
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp * scaleFactor))
            
            // SIA Official Header
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SIA - STRATEGIC INTELLIGENCE AGENCY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = (10 * scaleFactor).sp,
                        letterSpacing = (2 * scaleFactor).sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimaryCyan.copy(alpha = 0.6f)
                )
                Text(
                    text = "DIVISION: LOGIC & CRYPTOGRAPHY | CASE ID: #${session?.id?.take(8)?.uppercase() ?: "UNKNOWN"}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = (9 * scaleFactor).sp,
                        letterSpacing = (1 * scaleFactor).sp
                    ),
                    color = TextSecondary.copy(alpha = 0.4f)
                )
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp * scaleFactor),
                    thickness = 1.dp,
                    color = PrimaryCyan.copy(alpha = 0.15f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp * scaleFactor))

            if (session == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Session not found", color = ErrorRed)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp * scaleFactor),
                    contentPadding = PaddingValues(bottom = 32.dp * scaleFactor),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    session.levels.forEach { levelResult ->
                        item {
                            LevelHeader(levelResult, scaleFactor)
                        }
                        
                        items(levelResult.hints.size) { globalIndex ->
                            val hint = levelResult.hints[globalIndex]
                            
                            // Check if it's a user guess
                            val isUserGuess = hint.descriptionRes == com.brainfocus.numberdetective.R.string.log_analysis_attempt
                            
                            // Analysis numbering
                            val analysisNumber = if (isUserGuess) {
                                levelResult.hints.take(globalIndex + 1).count { it.descriptionRes == com.brainfocus.numberdetective.R.string.log_analysis_attempt }
                            } else {
                                0
                            }

                            // Intelligence numbering
                            val intelligenceNumber = if (!isUserGuess && hint.descriptionRes != com.brainfocus.numberdetective.R.string.log_analysis_success) {
                                levelResult.hints.take(globalIndex + 1).count { 
                                    it.descriptionRes != com.brainfocus.numberdetective.R.string.log_analysis_attempt && 
                                    it.descriptionRes != com.brainfocus.numberdetective.R.string.log_analysis_success 
                                }
                            } else {
                                0
                            }

                            ArchiveHintCard(
                                hint = hint, 
                                analysisNumber = analysisNumber, 
                                intelligenceNumber = intelligenceNumber,
                                scaleFactor = scaleFactor
                            )
                        }
                    }
                }
            }
            
        }
    }
}
