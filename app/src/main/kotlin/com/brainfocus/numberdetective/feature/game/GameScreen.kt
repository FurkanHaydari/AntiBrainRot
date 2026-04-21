package com.brainfocus.numberdetective.feature.game

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import com.brainfocus.numberdetective.data.model.GameState
import com.brainfocus.numberdetective.data.model.GuessResult
import com.brainfocus.numberdetective.data.model.Hint
import com.brainfocus.numberdetective.feature.home.RowDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onNavigateToResult: (Boolean, Int, String, Int, Int, Int, Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    LocalContext.current
    val currentLevel by viewModel.currentLevel.collectAsState()
    val remainingAttempts by viewModel.remainingAttempts.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val score by viewModel.score.collectAsState()
    val dailyHighScore by viewModel.dailyHighScore.collectAsState(0)
    val allTimeHighScore by viewModel.allTimeHighScore.collectAsState(0)
    val hints by viewModel.hints.collectAsState()
    val evidenceHints = hints.take(5)
    val trialHints = hints.drop(5)
    
    val gameState by viewModel.gameState.collectAsState()
    val correctAnswer by viewModel.correctAnswer.collectAsState()
    val currentReport by viewModel.currentReport.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val isHelperModeEnabled by viewModel.isHelperModeEnabled.collectAsState(initial = false)
    val countdownValue by viewModel.countdownValue.collectAsState()
    val attempts = viewModel.attempts

    val sheetState = rememberModalBottomSheetState()
    var showHistorySheet by remember { mutableStateOf(false) }

    val expectedLength = if (currentLevel == 3) 4 else 3
    var pickerValues by remember(currentLevel) {
        mutableStateOf(List(expectedLength) { 0 })
    }

    LaunchedEffect(gameState) {
        when (gameState) {
            is GameState.Win -> {
                onNavigateToResult(true, score, correctAnswer, attempts, viewModel.getTimeInSeconds(), dailyHighScore, allTimeHighScore)
            }
            is GameState.GameOver -> {
                onNavigateToResult(false, score, correctAnswer, attempts, viewModel.getTimeInSeconds(), dailyHighScore, allTimeHighScore)
            }
            else -> {}
        }
    }

    // Handle system back button
    BackHandler(enabled = gameState is GameState.Playing && currentReport == null) {
        viewModel.pauseGame()
    }

    // Handle lifecycle changes (phone calls, backgrounding)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.pauseGame()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scaleFactor = (maxHeight.value / 812f).coerceIn(1.0f, 2.2f)
        val maxWidth = maxWidth

        // --- Layer 1: Background ---
        Image(
            painter = painterResource(id = R.drawable.detective_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(if (currentReport != null) 8.dp else 0.dp), // Dynamic blur
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (currentReport != null) 0.9f else 0.85f))
        )

        // --- Layer 2: UI Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = (20.dp * scaleFactor).coerceAtMost(32.dp))
                .blur(if (currentReport != null) 20.dp else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameTopBar(level = currentLevel, scaleFactor = scaleFactor)
            Spacer(modifier = Modifier.height(10.dp * scaleFactor))

            StatsDashboard(
                attempts = remainingAttempts,
                time = remainingTime,
                trialCount = trialHints.size,
                scaleFactor = scaleFactor,
                onHistoryClick = { 
                    if (!isPaused) {
                        viewModel.recordArchiveOpen()
                        showHistorySheet = true 
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp * scaleFactor))

            Box(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Minimized Placeholder Header (Visible when not counting down)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .blur(if (countdownValue != null) 20.dp else 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 0.7f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryCyan.copy(alpha = alpha), modifier = Modifier.size(18.dp * scaleFactor))
                        Spacer(modifier = Modifier.width(8.dp * scaleFactor))
                        Text(
                            text = stringResource(R.string.msg_analysis_active),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = (11 * scaleFactor).coerceAtMost(16f).sp
                            ),
                            color = PrimaryCyan.copy(alpha = alpha),
                            letterSpacing = (2 * scaleFactor).sp
                        )
                    }

                    // Evidence List (Persistent)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(if (countdownValue != null) 20.dp else 0.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(evidenceHints) { index, hint ->
                            HintCard(hint = hint, isHelperModeEnabled = isHelperModeEnabled, scaleFactor = scaleFactor, index = index + 1)
                        }
                    }
                }

                // --- Layer 3: Countdown Overlay (Localized) ---
                androidx.compose.animation.AnimatedVisibility(
                    visible = countdownValue != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = countdownValue,
                            transitionSpec = {
                                (scaleIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeIn())
                                    .togetherWith(scaleOut(animationSpec = tween(300)) + fadeOut())
                            },
                            label = "CountdownAnimation"
                        ) { value ->
                            if (value != null) {
                                Text(
                                    text = if (value == 0) stringResource(R.string.countdown_go) else value.toString(),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = (80 * scaleFactor).coerceAtMost(160f).sp,
                                        fontFamily = Montserrat,
                                        letterSpacing = (4 * scaleFactor).sp
                                    ),
                                    color = PrimaryCyan,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp * scaleFactor))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pickerValues.forEachIndexed { index, value ->
                    NumberVaultPicker(
                        value = value,
                        scaleFactor = scaleFactor,
                        onValueChange = { newValue ->
                            if (newValue != pickerValues[index]) {
                                val newList = pickerValues.toMutableList()
                                newList[index] = newValue
                                pickerValues = newList
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp * scaleFactor))

            GuessButton(
                enabled = !isPaused,
                scaleFactor = scaleFactor,
                onClick = {
                    val guess = pickerValues.joinToString("")
                    if (guess.length != expectedLength) return@GuessButton
                    
                    // Trigger analysis & validation
                    val result = viewModel.makeGuess(guess)
                    
                    // Only reset picker if the guess was actually analyzed (not a validation error)
                    if (result != GuessResult.Invalid) {
                        pickerValues = List(expectedLength) { 0 }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp * scaleFactor))
        }

        // --- Layer 3: Case Archive Sheet ---
        if (showHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false },
                sheetState = sheetState,
                containerColor = SurfaceCard,
                scrimColor = Color.Black.copy(alpha = 0.7f),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_case_archive),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = Montserrat,
                            fontSize = (24 * scaleFactor).coerceAtMost(32f).sp
                        ),
                        color = PrimaryCyan,
                        modifier = Modifier.padding(bottom = 16.dp * scaleFactor)
                    )
                    
                    if (trialHints.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.msg_no_evidence_waiting),
                                color = TextSecondary.copy(alpha = 0.5f),
                                letterSpacing = 2.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(trialHints) { index, hint -> 
                                HintCard(hint = hint, isHelperModeEnabled = isHelperModeEnabled, scaleFactor = scaleFactor, index = index + 1) 
                            }
                        }
                    }
                }
            }
        }

        // --- Layer 4: Field Report Overlay ---
        AnimatedVisibility(
            visible = currentReport != null,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            currentReport?.let { report ->
                FieldReportOverlay(
                    report = report,
                    scaleFactor = scaleFactor,
                    maxWidth = maxWidth,
                    onDismiss = { viewModel.dismissReport() },
                    onExit = onNavigateBack,
                    remainingTime = remainingTime
                )
            }
        }

    }
}

@Composable
fun FieldReportOverlay(
    report: FieldReport, 
    scaleFactor: Float,
    maxWidth: androidx.compose.ui.unit.Dp,
    onDismiss: () -> Unit,
    onExit: () -> Unit,
    remainingTime: Int
) {
    val isPauseReport = report is FieldReport.Pause
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = (400.dp * scaleFactor).coerceAtMost(maxWidth))
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            color = SurfaceCard,
            shape = RoundedCornerShape(28.dp * scaleFactor),
            border = androidx.compose.foundation.BorderStroke(
                RowDefaults.CardBorder.width,
                RowDefaults.CardBorder.brush
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp * scaleFactor)
                        .background(
                            when {
                                isPauseReport -> PrimaryCyan.copy(alpha = 0.1f)
                                report.isPositive -> SuccessGreen.copy(alpha = 0.1f)
                                else -> ErrorRed.copy(alpha = 0.1f)
                            },
                            CircleShape
                        )
                        .border(
                            1.dp, 
                            when {
                                isPauseReport -> PrimaryCyan
                                report.isPositive -> SuccessGreen
                                else -> ErrorRed
                            }, 
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPauseReport) "⏸️" else if (report.isPositive) "🎖️" else "⚠️", 
                        fontSize = (32 * scaleFactor).coerceAtMost(48f).sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp * scaleFactor))

                Text(
                    text = stringResource(report.titleRes).uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = Montserrat, 
                        fontWeight = FontWeight.Bold, 
                        letterSpacing = (2 * scaleFactor).sp,
                        fontSize = (20 * scaleFactor).coerceAtMost(32f).sp
                    ),
                    color = if (isPauseReport) PrimaryCyan else if (report.isPositive) PrimaryCyan else ErrorRed,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp * scaleFactor))

                Text(
                    text = stringResource(report.messageRes, *report.messageArgs.toTypedArray()),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = (16 * scaleFactor).coerceAtMost(24f).sp,
                        lineHeight = (22 * scaleFactor).coerceAtMost(32f).sp
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                if (isPauseReport) {
                    Spacer(modifier = Modifier.height(16.dp * scaleFactor))
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp * scaleFactor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp * scaleFactor),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.label_time).uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = (12 * scaleFactor).coerceAtMost(18f).sp
                                ),
                                color = TextSecondary
                            )
                            Text(
                                text = String.format("%02d:%02d", remainingTime / 60, remainingTime % 60),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = (16 * scaleFactor).coerceAtMost(24f).sp
                                ),
                                color = PrimaryCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp * scaleFactor))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp * scaleFactor)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp * scaleFactor)
                            .border(1.dp, PrimaryCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp * scaleFactor)),
                        shape = RoundedCornerShape(16.dp * scaleFactor),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(PlayButtonGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(if (isPauseReport) R.string.resume_mission else R.string.continue_mission),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = Montserrat,
                                    letterSpacing = (1 * scaleFactor).sp,
                                    fontSize = (16 * scaleFactor).coerceAtMost(22f).sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    if (isPauseReport) {
                        OutlinedButton(
                            onClick = onExit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp * scaleFactor),
                            shape = RoundedCornerShape(16.dp * scaleFactor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            border = androidx.compose.foundation.BorderStroke(
                                RowDefaults.CardBorder.width,
                                RowDefaults.CardBorder.brush
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.exit_mission),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = Montserrat,
                                    letterSpacing = (1 * scaleFactor).sp,
                                    fontSize = (16 * scaleFactor).coerceAtMost(22f).sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun GameTopBar(level: Int, scaleFactor: Float) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp * scaleFactor),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.case_file_level, level).uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = Montserrat,
                letterSpacing = (2 * scaleFactor).sp,
                fontSize = (14 * scaleFactor).coerceAtMost(22f).sp,
                fontWeight = FontWeight.Bold
            ),
            color = PrimaryCyan.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun StatsDashboard(attempts: Int, time: Int, trialCount: Int, scaleFactor: Float, onHistoryClick: () -> Unit) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(20.dp * scaleFactor),
        border = androidx.compose.foundation.BorderStroke(
            RowDefaults.CardBorder.width,
            RowDefaults.CardBorder.brush
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp * scaleFactor).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(label = stringResource(R.string.label_lives), value = "x$attempts", color = ErrorRed, scaleFactor = scaleFactor)
            VerticalDivider(modifier = Modifier.height(24.dp * scaleFactor).width(1.dp), color = Color.White.copy(alpha = 0.1f))
            StatItem(label = stringResource(R.string.label_time), value = String.format("%02d:%02d", time / 60, time % 60), color = PrimaryCyan, scaleFactor = scaleFactor)
            VerticalDivider(modifier = Modifier.height(24.dp * scaleFactor).width(1.dp), color = Color.White.copy(alpha = 0.1f))
            StatItem(
                label = stringResource(R.string.label_trials), 
                value = trialCount.toString(), 
                color = SuccessGreen,
                scaleFactor = scaleFactor,
                onClick = onHistoryClick
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color, scaleFactor: Float, onClick: (() -> Unit)? = null) {
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(value) {
        if (onClick != null) {
            scale.animateTo(1.15f, tween(150))
            scale.animateTo(1f, tween(150))
        }
    }

    val baseModifier = Modifier
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }

    val finalModifier = if (onClick != null) {
        baseModifier
            .clip(RoundedCornerShape(8.dp * scaleFactor))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp * scaleFactor, vertical = 4.dp * scaleFactor)
    } else {
        baseModifier.padding(horizontal = 12.dp * scaleFactor, vertical = 4.dp * scaleFactor)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = finalModifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = (10 * scaleFactor).coerceAtMost(16f).sp)
            if (onClick != null) {
                Spacer(modifier = Modifier.width(4.dp * scaleFactor))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.8f),
                    modifier = Modifier.size(10.dp * scaleFactor)
                )
            }
        }
        Text(
            text = value, 
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = (16 * scaleFactor).coerceAtMost(24f).sp
            ), 
            color = color, 
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HintCard(hint: Hint, isHelperModeEnabled: Boolean, scaleFactor: Float, index: Int) {
    val isGuess = hint.timestamp != null && hint.timestamp > 0
    
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp * scaleFactor),
        border = androidx.compose.foundation.BorderStroke(
            RowDefaults.CardBorder.width,
            RowDefaults.CardBorder.brush
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp * scaleFactor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: INTEL #N or ANALYSIS #N
            Text(
                text = if (isGuess) {
                    stringResource(com.brainfocus.numberdetective.R.string.log_analysis_number, index)
                } else {
                    stringResource(com.brainfocus.numberdetective.R.string.initial_intelligence_number, index)
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (11 * scaleFactor).coerceAtMost(16f).sp,
                    letterSpacing = (1.5f * scaleFactor).sp
                ),
                color = if (isGuess) PrimaryCyan.copy(alpha = 0.7f) else TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 6.dp * scaleFactor)
            )

            // Digits Array
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp * scaleFactor)) {
                    hint.guess.forEachIndexed { digitIndex, char ->
                        val status = if (isHelperModeEnabled) hint.digitStatuses?.getOrNull(digitIndex) else null
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
                                .size(34.dp * scaleFactor)
                                .background(bgColor, RoundedCornerShape(8.dp * scaleFactor))
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp * scaleFactor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = (16 * scaleFactor).coerceAtMost(24f).sp
                                ),
                                color = if (status != null) Color.White else PrimaryCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp * scaleFactor))

            val hintText = if (hint.descriptionRes != null) {
                stringResource(hint.descriptionRes, *hint.descriptionArgs.toTypedArray())
            } else {
                hint.description
            }
            
            Text(
                text = hintText, 
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (15 * scaleFactor).coerceAtMost(22f).sp,
                    lineHeight = (20 * scaleFactor).coerceAtMost(28f).sp,
                    fontWeight = FontWeight.Medium
                ), 
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NumberVaultPicker(value: Int, scaleFactor: Float, onValueChange: (Int) -> Unit) {
    val pageCount = 10000
    val startIndex = 5000 - (5000 % 10) + value
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = startIndex,
        pageCount = { pageCount }
    )

    // Always hold the latest callback to avoid stale lambda in long-lived effects
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    // Haptic feedback for premium feel
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    // Sync value on every settled page change (including initial)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { page ->
                currentOnValueChange(page % 10)
            }
    }

    // Haptic tick on every page scroll (like iOS wheel)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            }
    }

    // React to external changes (reset after guess)
    LaunchedEffect(value) {
        val currVal = pagerState.settledPage % 10
        if (currVal != value) {
            var diff = value - currVal
            if (diff > 5) diff -= 10
            if (diff < -5) diff += 10
            pagerState.animateScrollToPage(pagerState.settledPage + diff)
        }
    }

    val fling = androidx.compose.foundation.pager.PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = androidx.compose.foundation.pager.PagerSnapDistance.atMost(4),
        snapPositionalThreshold = 0.4f,
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    androidx.compose.foundation.pager.VerticalPager(
        state = pagerState,
        modifier = Modifier
            .width(64.dp * scaleFactor)
            .height(110.dp * scaleFactor)
            .background(SurfaceCard, RoundedCornerShape(16.dp * scaleFactor))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp * scaleFactor)),
        contentPadding = PaddingValues(vertical = 30.dp * scaleFactor),
        beyondViewportPageCount = 2,
        flingBehavior = fling
    ) { page ->
        val itemValue = page % 10
        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

        // iOS-style 3D cylinder rotation
        val rotationX = pageOffset * -30f  // Tilt away like a wheel
        val scale = 1f - (kotlin.math.abs(pageOffset).coerceIn(0f, 1f) * 0.25f)
        val alpha = 1f - (kotlin.math.abs(pageOffset).coerceIn(0f, 1f) * 0.6f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp * scaleFactor)
                .graphicsLayer {
                    this.rotationX = rotationX
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    // Perspective depth scaled
                    cameraDistance = (12f * scaleFactor) * density
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = itemValue.toString(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = (32 * scaleFactor).coerceAtMost(48f).sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GuessButton(enabled: Boolean, scaleFactor: Float, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp * scaleFactor)
            .border(1.dp, PrimaryCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp * scaleFactor)),
        shape = RoundedCornerShape(16.dp * scaleFactor),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent)
    ) {
        Box(
            modifier = if (enabled) {
                Modifier.fillMaxSize().background(PlayButtonGradient)
            } else {
                Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.05f))
            },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.submit_button).uppercase(), 
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = Montserrat, 
                    fontSize = (16 * scaleFactor).coerceAtMost(24f).sp,
                    letterSpacing = (2 * scaleFactor).sp
                ), 
                color = if (enabled) Color.White else Color.Gray
            )
        }
    }
}
