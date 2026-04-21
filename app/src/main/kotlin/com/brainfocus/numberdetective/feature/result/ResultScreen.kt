package com.brainfocus.numberdetective.feature.result

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import com.brainfocus.numberdetective.core.utils.ShareImageGenerator
import com.brainfocus.numberdetective.data.storage.GameResultStorage

@Composable
fun ResultScreen(
    isWin: Boolean,
    score: Int,
    correctAnswer: String,
    attempts: Int,
    timeInSeconds: Int,
    dailyHighScore: Int,
    allTimeHighScore: Int,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Briefing, 1: Archive
    val coroutineScope = rememberCoroutineScope()

    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    LaunchedEffect(Unit) {
        isVisible = true
    }

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
                    if (isWin) {
                        Brush.radialGradient(
                            colors = listOf(PrimaryCyan.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.9f)),
                            radius = 1500f
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), ErrorRed.copy(alpha = 0.1f), Color.Black.copy(alpha = 0.95f))
                        )
                    }
                )
        )

        // --- Layer 2: Main Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp * scaleFactor))

            // Cinematic Header
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { -it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(if (isWin) R.string.mission_accomplished else R.string.mission_failed).uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = Montserrat,
                            fontSize = (28 * scaleFactor).coerceAtMost(42f).sp,
                            letterSpacing = (2 * scaleFactor).sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = if (isWin) PrimaryCyan else ErrorRed,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp * scaleFactor))
                    Text(
                        text = (if (isWin) stringResource(R.string.win_motivation) else stringResource(R.string.lose_motivation)).uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            letterSpacing = (1.5f * scaleFactor).sp,
                            fontWeight = FontWeight.Medium,
                            fontSize = (11 * scaleFactor).coerceAtMost(16f).sp
                        ),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp * scaleFactor))

            // Custom Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 450.dp * scaleFactor)
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

            Spacer(modifier = Modifier.height(20.dp * scaleFactor))

            // Tab Content
            Crossfade(
                targetState = selectedTab, 
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "TabContentTransition"
            ) { tab ->
                when (tab) {
                    0 -> BriefingView(
                        isWin = isWin,
                        score = score,
                        correctAnswer = correctAnswer,
                        attempts = attempts,
                        formattedTime = formattedTime,
                        dailyHighScore = dailyHighScore,
                        allTimeHighScore = allTimeHighScore,
                        scaleFactor = scaleFactor,
                        maxWidth = maxWidthDp
                    )
                    1 -> CaseArchiveView(scaleFactor = scaleFactor)
                }
            }

            Spacer(modifier = Modifier.height(20.dp * scaleFactor))

            // Action Buttons
            Column(
                modifier = Modifier
                    .widthIn(max = (400.dp * scaleFactor).coerceAtMost(maxWidthDp))
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp * scaleFactor)
            ) {
                // Share
                DetectiveButton(
                    text = stringResource(R.string.share_button),
                    isPrimary = false,
                    scaleFactor = scaleFactor,
                    onClick = {
                        val playStoreLink = "https://play.google.com/store/apps/details?id=${context.packageName}"
                        val baseMessage = context.getString(R.string.share_score_message, score, attempts, formattedTime)
                        val shareMessage = "$baseMessage\n\n$playStoreLink"
                        val shareTitle = context.getString(R.string.share_score_title)
                        
                        coroutineScope.launch(Dispatchers.IO) {
                            val imageUri = ShareImageGenerator.generateShareImage(context, isWin, score)
                            
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                if (imageUri != null) {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, imageUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                } else {
                                    type = "text/plain"
                                }
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                            }
                            
                            withContext(Dispatchers.Main) {
                                context.startActivity(Intent.createChooser(shareIntent, shareTitle))
                            }
                        }
                    }
                )

                // Play Again
                DetectiveButton(
                    text = stringResource(R.string.play_again_button),
                    isPrimary = true,
                    scaleFactor = scaleFactor,
                    onClick = onPlayAgain
                )

                TextButton(
                    onClick = onGoHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.back_to_menu).uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            letterSpacing = (1.5 * scaleFactor).sp,
                            fontSize = (12 * scaleFactor).coerceAtMost(16f).sp
                        ),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, scaleFactor: Float, modifier: Modifier = Modifier, onClick: () -> Unit) {
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

@Composable
fun BriefingView(
    isWin: Boolean,
    score: Int,
    correctAnswer: String,
    attempts: Int,
    formattedTime: String,
    dailyHighScore: Int,
    allTimeHighScore: Int,
    scaleFactor: Float,
    maxWidth: androidx.compose.ui.unit.Dp
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(24.dp * scaleFactor),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier
                .widthIn(max = (450.dp * scaleFactor).coerceAtMost(maxWidth))
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp * scaleFactor),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.final_score).uppercase(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (10 * scaleFactor).coerceAtMost(14f).sp
                    ),
                    color = TextSecondary,
                    letterSpacing = (2 * scaleFactor).sp
                )
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = (42 * scaleFactor).coerceAtMost(60f).sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimaryCyan
                )
                
                // Records Summary
                Spacer(modifier = Modifier.height(8.dp * scaleFactor))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(10.dp * scaleFactor))
                        .padding(8.dp * scaleFactor),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DetectiveStatItem(label = stringResource(R.string.label_daily_record), value = dailyHighScore.toString(), color = Color.White.copy(alpha = 0.9f), scaleFactor = scaleFactor * 0.9f)
                    DetectiveStatItem(label = stringResource(R.string.label_all_time_record), value = allTimeHighScore.toString(), color = Color.White.copy(alpha = 0.9f), scaleFactor = scaleFactor * 0.9f)
                }
                
                // SIA Evaluation Section (Integrated)
                Spacer(modifier = Modifier.height(16.dp * scaleFactor))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp * scaleFactor))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(10.dp * scaleFactor))
                        .padding(10.dp * scaleFactor),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SIA-7 COGNITIVE EVALUATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = (9 * scaleFactor).sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (2 * scaleFactor).sp
                        ),
                        color = PrimaryCyan.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(6.dp * scaleFactor))
                    Text(
                        text = stringResource(if (isWin) R.string.sia_evaluation_win else R.string.sia_evaluation_loss),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = (11 * scaleFactor).coerceAtMost(15f).sp,
                            lineHeight = (16 * scaleFactor).coerceAtMost(22f).sp
                        ),
                        color = TextSecondary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CaseArchiveView(scaleFactor: Float) {
    val session = GameResultStorage.lastGameSession
    
    if (session == null || session.levels.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.msg_no_evidence_waiting),
                color = TextSecondary.copy(alpha = 0.5f),
                letterSpacing = 2.sp
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp * scaleFactor),
        contentPadding = PaddingValues(bottom = 32.dp * scaleFactor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        session.levels.forEach { levelResult ->
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
                                fontSize = (16 * scaleFactor).coerceAtMost(24f).sp
                            ),
                            color = SuccessGreen
                        )
                    }
                )
            }
            
            items(levelResult.hints.size) { globalIndex ->
                val hint = levelResult.hints[globalIndex]
                val isUserGuess = hint.descriptionRes == R.string.log_analysis_attempt
                
                val label = if (hint.descriptionRes == R.string.log_analysis_success) {
                    stringResource(R.string.log_analysis_success)
                } else if (isUserGuess) {
                    val interrogationNumber = levelResult.hints.take(globalIndex + 1).count { it.descriptionRes == R.string.log_analysis_attempt }
                    stringResource(R.string.log_interrogation_number, interrogationNumber)
                } else {
                    val intelligenceNumber = levelResult.hints.take(globalIndex + 1).count { 
                        it.descriptionRes != R.string.log_analysis_attempt && 
                        it.descriptionRes != R.string.log_analysis_success 
                    }
                    stringResource(R.string.initial_intelligence_number, intelligenceNumber)
                }

                DetectiveHintCard(
                    hint = hint,
                    isHelperModeEnabled = true,
                    scaleFactor = scaleFactor,
                    label = label,
                    labelColor = if (hint.descriptionRes == R.string.log_analysis_success) SuccessGreen 
                                 else if (isUserGuess) PrimaryCyan 
                                 else TextSecondary.copy(alpha = 0.6f),
                    maxWidth = 550.dp * scaleFactor
                )
            }
        }
    }
}
