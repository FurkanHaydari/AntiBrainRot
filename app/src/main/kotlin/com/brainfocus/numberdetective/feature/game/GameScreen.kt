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

    Box(modifier = Modifier.fillMaxSize()) {
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
                .padding(horizontal = 20.dp)
                .blur(if (currentReport != null) 20.dp else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameTopBar(level = currentLevel)
            Spacer(modifier = Modifier.height(16.dp))

            StatsDashboard(
                attempts = remainingAttempts,
                time = remainingTime,
                trialCount = trialHints.size,
                onHistoryClick = { 
                    if (!isPaused) {
                        viewModel.recordArchiveOpen()
                        showHistorySheet = true 
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Minimized Placeholder Header (Visible when not counting down)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
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
                        Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryCyan.copy(alpha = alpha), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.msg_analysis_active),
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryCyan.copy(alpha = alpha),
                            letterSpacing = 2.sp
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
                        items(evidenceHints) { hint ->
                            HintCard(hint = hint, isHelperModeEnabled = isHelperModeEnabled)
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
                                        fontSize = 80.sp,
                                        fontFamily = Montserrat,
                                        letterSpacing = 4.sp
                                    ),
                                    color = PrimaryCyan,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pickerValues.forEachIndexed { index, value ->
                    NumberVaultPicker(
                        value = value,
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

            Spacer(modifier = Modifier.height(20.dp))

            GuessButton(
                enabled = !isPaused,
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
            Spacer(modifier = Modifier.height(16.dp))
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
                        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = Montserrat),
                        color = PrimaryCyan,
                        modifier = Modifier.padding(bottom = 16.dp)
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
                            items(trialHints) { hint -> 
                                HintCard(hint = hint, isHelperModeEnabled = isHelperModeEnabled) 
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
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            color = SurfaceCard,
            shape = RoundedCornerShape(28.dp),
            border = RowDefaults.CardBorder
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
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
                    Text(text = if (isPauseReport) "⏸️" else if (report.isPositive) "🎖️" else "⚠️", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(report.titleRes).uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    color = if (isPauseReport) PrimaryCyan else if (report.isPositive) PrimaryCyan else ErrorRed,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(report.messageRes, *report.messageArgs.toTypedArray()),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                if (isPauseReport) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.label_time).uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = String.format("%02d:%02d", remainingTime / 60, remainingTime % 60),
                                style = MaterialTheme.typography.titleMedium,
                                color = PrimaryCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(1.dp, PrimaryCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
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
                                    letterSpacing = 1.sp
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
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            border = RowDefaults.CardBorder
                        ) {
                            Text(
                                text = stringResource(R.string.exit_mission),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = Montserrat,
                                    letterSpacing = 1.sp
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
fun GameTopBar(level: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.case_file_level, level).uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = Montserrat,
                letterSpacing = 2.sp,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            color = PrimaryCyan.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun StatsDashboard(attempts: Int, time: Int, trialCount: Int, onHistoryClick: () -> Unit) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(20.dp),
        border = RowDefaults.CardBorder,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(label = stringResource(R.string.label_lives), value = "x$attempts", color = ErrorRed)
            VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(alpha = 0.1f))
            StatItem(label = stringResource(R.string.label_time), value = String.format("%02d:%02d", time / 60, time % 60), color = PrimaryCyan)
            VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(alpha = 0.1f))
            StatItem(
                label = stringResource(R.string.label_trials), 
                value = trialCount.toString(), 
                color = SuccessGreen,
                onClick = onHistoryClick
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color, onClick: (() -> Unit)? = null) {
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
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    } else {
        baseModifier.padding(horizontal = 12.dp, vertical = 4.dp)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = finalModifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 10.sp)
            if (onClick != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.8f),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HintCard(hint: Hint, isHelperModeEnabled: Boolean) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = RowDefaults.CardBorder,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                hint.guess.forEachIndexed { index, char ->
                    val status = if (isHelperModeEnabled) hint.digitStatuses?.getOrNull(index) else null
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
                            .size(34.dp)
                            .background(bgColor, RoundedCornerShape(8.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (status != null) Color.White else PrimaryCyan
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            val hintText = if (hint.descriptionRes != null) {
                stringResource(hint.descriptionRes, *hint.descriptionArgs.toTypedArray())
            } else {
                hint.description
            }
            Text(text = hintText, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f), lineHeight = 18.sp)
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NumberVaultPicker(value: Int, onValueChange: (Int) -> Unit) {
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
            .width(64.dp)
            .height(130.dp)
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        contentPadding = PaddingValues(vertical = 40.dp),
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
                .height(50.dp)
                .graphicsLayer {
                    this.rotationX = rotationX
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    // Perspective depth
                    cameraDistance = 12f * density
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = itemValue.toString(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GuessButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp).border(1.dp, PrimaryCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
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
            Text(text = stringResource(R.string.submit_button).uppercase(), style = MaterialTheme.typography.titleMedium.copy(fontFamily = Montserrat, letterSpacing = 2.sp), color = if (enabled) Color.White else Color.Gray)
        }
    }
}
