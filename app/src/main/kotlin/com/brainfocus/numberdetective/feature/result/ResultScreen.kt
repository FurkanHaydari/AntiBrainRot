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
                            fontSize = 28.sp,
                            letterSpacing = 2.sp
                        ),
                        color = if (isWin) PrimaryCyan else ErrorRed,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (if (isWin) stringResource(R.string.win_motivation) else stringResource(R.string.lose_motivation)).uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Light
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
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                TabItem(
                    text = stringResource(R.string.label_tab_briefing),
                    isSelected = selectedTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 0 }
                )
                TabItem(
                    text = stringResource(R.string.label_tab_archive),
                    isSelected = selectedTab == 1,
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
                        allTimeHighScore = allTimeHighScore
                    )
                    1 -> CaseArchiveView()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Share
                ResultActionButton(
                    text = stringResource(R.string.share_button).uppercase(),
                    isPrimary = false,
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
                    onClick = onPlayAgain
                )

                TextButton(
                    onClick = onGoHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.back_to_menu).uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 1.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PrimaryCyan.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 1.sp
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
    allTimeHighScore: Int
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(28.dp),
            border = RowDefaults.CardBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.final_score),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimaryCyan
                )
                
                // Records Summary
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RecordItem(label = stringResource(R.string.label_daily_record), value = dailyHighScore)
                    RecordItem(label = stringResource(R.string.label_all_time_record), value = allTimeHighScore)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (!isWin) {
                        DebriefStat(stringResource(R.string.correct_answer), correctAnswer)
                    }
                    DebriefStat(stringResource(R.string.attempts), attempts.toString())
                    DebriefStat(stringResource(R.string.time), formattedTime)
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))

        // Philosophical Insight
        val quotes = stringArrayResource(id = R.array.game_quotes)
        val selectedQuote = remember { quotes.random() }
        
        Column(
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedQuote,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 18.sp
                    ),
                    color = TextSecondary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun CaseArchiveView() {
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        session.levels.forEach { levelResult ->
            item {
                LevelHeader(levelResult)
            }
            
            items(levelResult.hints.size) { globalIndex ->
                val hint = levelResult.hints[globalIndex]
                
                // Check if it's a user guess using the resource ID
                val isUserGuess = hint.descriptionRes == R.string.log_analysis_attempt
                
                // To find the actual number (1, 2, 3...) of this specific analysis
                val analysisNumber = if (isUserGuess) {
                    // Determine how many preceding items were user guesses
                    levelResult.hints.take(globalIndex + 1).count { it.descriptionRes == R.string.log_analysis_attempt }
                } else {
                    0
                }

                ArchiveHintCard(hint = hint, analysisNumber = analysisNumber)
            }
        }
    }
}

@Composable
fun LevelHeader(levelResult: com.brainfocus.numberdetective.data.storage.LevelResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
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
                        letterSpacing = 2.sp
                    ),
                    color = PrimaryCyan
                )
                Text(
                    text = stringResource(R.string.score_points, levelResult.scoreGained),
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = levelResult.secretNumber,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = Poppins
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
fun ArchiveHintCard(hint: com.brainfocus.numberdetective.data.model.Hint, analysisNumber: Int) {
    val isUserGuess = hint.descriptionRes == R.string.log_analysis_attempt || hint.descriptionRes == R.string.log_analysis_success

    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = RowDefaults.CardBorder,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // header logic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (hint.descriptionRes == R.string.log_analysis_success) {
                        stringResource(R.string.log_analysis_success).uppercase()
                    } else if (isUserGuess) {
                        stringResource(R.string.log_analysis_number, analysisNumber)
                    } else {
                        stringResource(R.string.initial_intelligence)
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (hint.descriptionRes == R.string.log_analysis_success) SuccessGreen 
                            else if (isUserGuess) PrimaryCyan 
                            else TextSecondary,
                    letterSpacing = 1.sp
                )
                if (hint.timestamp != null && hint.timestamp > 0) {
                    val m = hint.timestamp / 60
                    val s = hint.timestamp % 60
                    Text(
                        text = String.format("T+%02d:%02d", m, s),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = Poppins),
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Digits Array
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                hint.guess.forEachIndexed { charIndex, char ->
                    val status = hint.digitStatuses?.getOrNull(charIndex)
                    val bgColor = when (status) {
                        com.brainfocus.numberdetective.data.model.DigitStatus.CORRECT_POS -> SuccessGreen.copy(alpha = 0.2f)
                        com.brainfocus.numberdetective.data.model.DigitStatus.WRONG_POS -> WarningYellow.copy(alpha = 0.2f)
                        com.brainfocus.numberdetective.data.model.DigitStatus.INCORRECT -> ErrorRed.copy(alpha = 0.2f)
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
                            .size(38.dp)
                            .background(bgColor, RoundedCornerShape(8.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (status != null) Color.White else PrimaryCyan
                        )
                    }
                }

                val hintDesc = if (hint.descriptionRes != null) {
                    stringResource(hint.descriptionRes, *hint.descriptionArgs.toTypedArray())
                } else {
                    hint.description
                }

                if (hintDesc.isNotEmpty() && !isUserGuess) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = hintDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary.copy(alpha = 0.9f),
                        modifier = Modifier.align(Alignment.CenterVertically),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DebriefStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
fun ResultActionButton(text: String, isPrimary: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                1.dp,
                if (isPrimary) PrimaryCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
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
                    letterSpacing = 1.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun RecordItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary.copy(alpha = 0.6f),
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}
