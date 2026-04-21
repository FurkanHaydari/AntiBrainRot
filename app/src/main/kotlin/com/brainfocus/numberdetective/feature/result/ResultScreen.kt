package com.brainfocus.numberdetective.feature.result

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.brainfocus.numberdetective.feature.home.RowDefaults
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
            Spacer(modifier = Modifier.height(16.dp))

            // Cinematic Header
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { -it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(if (isWin) R.string.mission_accomplished else R.string.mission_failed),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = Montserrat,
                            fontSize = (36 * scaleFactor).coerceAtMost(54f).sp,
                            letterSpacing = (2 * scaleFactor).sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = if (isWin) PrimaryCyan else ErrorRed,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp * scaleFactor))
                    Text(
                        text = (if (isWin) stringResource(R.string.win_motivation) else stringResource(R.string.lose_motivation)).uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            letterSpacing = (1.5f * scaleFactor).sp,
                            fontWeight = FontWeight.Medium,
                            fontSize = (15 * scaleFactor).coerceAtMost(22f).sp
                        ),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(16.dp * scaleFactor))

            // Action Buttons
            Column(
                modifier = Modifier
                    .widthIn(max = (400.dp * scaleFactor).coerceAtMost(maxWidthDp))
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp * scaleFactor)
            ) {
                // Share
                ResultActionButton(
                    text = stringResource(R.string.share_button).uppercase(),
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
                ResultActionButton(
                    text = stringResource(R.string.play_again_button).uppercase(),
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
                            letterSpacing = (1 * scaleFactor).sp,
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
                letterSpacing = (1 * scaleFactor).sp,
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
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(28.dp * scaleFactor),
            border = RowDefaults.CardBorder,
            modifier = Modifier
                .widthIn(max = (450.dp * scaleFactor).coerceAtMost(maxWidth))
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp * scaleFactor),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.final_score),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (12 * scaleFactor).coerceAtMost(18f).sp
                    ),
                    color = TextSecondary,
                    letterSpacing = (2 * scaleFactor).sp
                )
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = (48 * scaleFactor).coerceAtMost(72f).sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimaryCyan
                )
                
                // Records Summary
                Spacer(modifier = Modifier.height(16.dp * scaleFactor))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp * scaleFactor))
                        .padding(12.dp * scaleFactor),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RecordItem(label = stringResource(R.string.label_daily_record), value = dailyHighScore, scaleFactor = scaleFactor)
                    RecordItem(label = stringResource(R.string.label_all_time_record), value = allTimeHighScore, scaleFactor = scaleFactor)
                }
                
                Spacer(modifier = Modifier.height(24.dp * scaleFactor))
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                
                Spacer(modifier = Modifier.height(24.dp * scaleFactor))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (!isWin) {
                        DebriefStat(stringResource(R.string.correct_answer), correctAnswer, scaleFactor)
                    }
                    DebriefStat(stringResource(R.string.attempts), attempts.toString(), scaleFactor)
                    DebriefStat(stringResource(R.string.time), formattedTime, scaleFactor)
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))

        // Philosophical Insight
        val quotes = stringArrayResource(id = R.array.game_quotes)
        val selectedQuote = remember { quotes.random() }
        
        Column(
            modifier = Modifier
                .widthIn(max = (450.dp * scaleFactor).coerceAtMost(maxWidth))
                .padding(bottom = 24.dp * scaleFactor)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(12.dp * scaleFactor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedQuote,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = (12 * scaleFactor).coerceAtMost(16f).sp,
                        lineHeight = (18 * scaleFactor).coerceAtMost(24f).sp
                    ),
                    color = TextSecondary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp * scaleFactor)
                )
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
        contentPadding = PaddingValues(bottom = 24.dp * scaleFactor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        session.levels.forEach { levelResult ->
            item {
                LevelHeader(levelResult, scaleFactor)
            }
            
            items(levelResult.hints.size) { globalIndex ->
                val hint = levelResult.hints[globalIndex]
                
                // Check if it's a user guess using the resource ID
                val isUserGuess = hint.descriptionRes == R.string.log_analysis_attempt
                
                // Determine how many preceding items were user guesses
                val analysisNumber = if (isUserGuess) {
                    levelResult.hints.take(globalIndex + 1).count { it.descriptionRes == R.string.log_analysis_attempt }
                } else {
                    0
                }

                // Determine how many preceding items were initial intelligence
                val intelligenceNumber = if (!isUserGuess && hint.descriptionRes != R.string.log_analysis_success) {
                    levelResult.hints.take(globalIndex + 1).count { 
                        it.descriptionRes != R.string.log_analysis_attempt && 
                        it.descriptionRes != R.string.log_analysis_success 
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

@Composable
fun LevelHeader(levelResult: com.brainfocus.numberdetective.data.storage.LevelResult, scaleFactor: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp * scaleFactor, bottom = 4.dp * scaleFactor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.case_file_level, levelResult.levelNumber).uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (2 * scaleFactor).sp,
                        fontSize = (14 * scaleFactor).coerceAtMost(20f).sp
                    ),
                    color = PrimaryCyan
                )
                Text(
                    text = stringResource(R.string.score_points, levelResult.scoreGained),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = (11 * scaleFactor).coerceAtMost(16f).sp
                    ),
                    color = SuccessGreen.copy(alpha = 0.8f),
                    letterSpacing = (1 * scaleFactor).sp
                )
            }
            Text(
                text = levelResult.secretNumber,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = Poppins,
                    fontSize = (16 * scaleFactor).coerceAtMost(24f).sp
                ),
                color = SuccessGreen
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            color = Color.White.copy(alpha = 0.1f),
            thickness = 1.dp
        )
    }
}

@Composable
fun ArchiveHintCard(hint: com.brainfocus.numberdetective.data.model.Hint, analysisNumber: Int, intelligenceNumber: Int, scaleFactor: Float) {
    val isUserGuess = hint.descriptionRes == R.string.log_analysis_attempt || hint.descriptionRes == R.string.log_analysis_success

    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp * scaleFactor),
        border = RowDefaults.CardBorder,
        modifier = Modifier.widthIn(max = (550.dp * scaleFactor)).fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp * scaleFactor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: INTEL #N or ANALYSIS #N or ACCESS GRANTED
            Text(
                text = if (hint.descriptionRes == R.string.log_analysis_success) {
                    stringResource(R.string.log_analysis_success).uppercase()
                } else if (isUserGuess) {
                    stringResource(R.string.log_analysis_number, analysisNumber)
                } else {
                    stringResource(R.string.initial_intelligence_number, intelligenceNumber)
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (11 * scaleFactor).coerceAtMost(16f).sp
                ),
                color = if (hint.descriptionRes == R.string.log_analysis_success) SuccessGreen 
                        else if (isUserGuess) PrimaryCyan 
                        else TextSecondary.copy(alpha = 0.6f),
                letterSpacing = (1.5 * scaleFactor).sp,
                modifier = Modifier.padding(bottom = 12.dp * scaleFactor)
            )

            // Digits Array (Horizontal and Centered)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp * scaleFactor)) {
                    hint.guess.forEachIndexed { charIndex, char ->
                        val status = hint.digitStatuses?.getOrNull(charIndex)
                        val bgColor = when (status) {
                            com.brainfocus.numberdetective.data.model.DigitStatus.CORRECT_POS -> SuccessGreen.copy(alpha = 0.15f)
                            com.brainfocus.numberdetective.data.model.DigitStatus.WRONG_POS -> WarningYellow.copy(alpha = 0.15f)
                            com.brainfocus.numberdetective.data.model.DigitStatus.INCORRECT -> ErrorRed.copy(alpha = 0.15f)
                            else -> Color.White.copy(alpha = 0.05f)
                        }
                        val borderColor = when (status) {
                            com.brainfocus.numberdetective.data.model.DigitStatus.CORRECT_POS -> SuccessGreen
                            com.brainfocus.numberdetective.data.model.DigitStatus.WRONG_POS -> WarningYellow
                            com.brainfocus.numberdetective.data.model.DigitStatus.INCORRECT -> ErrorRed
                            else -> Color.White.copy(alpha = 0.1f)
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp * scaleFactor)
                                .background(bgColor, RoundedCornerShape(10.dp * scaleFactor))
                                .border(1.dp, borderColor, RoundedCornerShape(10.dp * scaleFactor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (16 * scaleFactor).coerceAtMost(24f).sp
                                ),
                                color = if (status != null) Color.White else PrimaryCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp * scaleFactor))

            val hintDesc = if (hint.descriptionRes != null) {
                stringResource(hint.descriptionRes, *hint.descriptionArgs.toTypedArray())
            } else {
                hint.description
            }
            
            Text(
                text = hintDesc,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (15 * scaleFactor).coerceAtMost(22f).sp,
                    lineHeight = (22 * scaleFactor).coerceAtMost(30f).sp,
                    fontWeight = FontWeight.Medium
                ),
                color = TextPrimary.copy(alpha = 0.9f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp * scaleFactor),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun DebriefStat(label: String, value: String, scaleFactor: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = (12 * scaleFactor).coerceAtMost(16f).sp
            ),
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (20 * scaleFactor).coerceAtMost(28f).sp
            ),
            color = Color.White
        )
    }
}

@Composable
fun ResultActionButton(text: String, isPrimary: Boolean, scaleFactor: Float, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp * scaleFactor)
            .border(
                1.dp,
                if (isPrimary) PrimaryCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp * scaleFactor)
            ),
        shape = RoundedCornerShape(16.dp * scaleFactor),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isPrimary) PlayButtonGradient else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.02f)))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = Montserrat,
                    letterSpacing = (1 * scaleFactor).sp,
                    fontSize = (16 * scaleFactor).coerceAtMost(22f).sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun RecordItem(label: String, value: Int, scaleFactor: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = (13 * scaleFactor).coerceAtMost(18f).sp
            ),
            color = TextSecondary.copy(alpha = 0.6f),
            letterSpacing = (1 * scaleFactor).sp
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (20 * scaleFactor).coerceAtMost(28f).sp
            ),
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}
