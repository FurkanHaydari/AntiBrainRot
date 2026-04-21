package com.brainfocus.numberdetective.feature.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
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
import com.brainfocus.numberdetective.data.model.FieldReport
import com.brainfocus.numberdetective.core.designsystem.FieldReportOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onNavigateToResult: (Boolean, Int, String, Int, Int, Int, Int, Int, Boolean, Int) -> Unit,
    onNavigateBack: () -> Unit
) {
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
    val logicalMistakes by viewModel.logicalMistakesCount.collectAsState()
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
                val totalHintsFound = hints.sumOf { it.correct + it.misplaced }
                onNavigateToResult(true, score, correctAnswer, attempts, viewModel.getTimeInSeconds(), dailyHighScore, allTimeHighScore, totalHintsFound, isHelperModeEnabled, logicalMistakes)
            }
            is GameState.GameOver -> {
                val totalHintsFound = hints.sumOf { it.correct + it.misplaced }
                onNavigateToResult(false, score, correctAnswer, attempts, viewModel.getTimeInSeconds(), dailyHighScore, allTimeHighScore, totalHintsFound, isHelperModeEnabled, logicalMistakes)
            }
            else -> {}
        }
    }

    BackHandler(enabled = gameState is GameState.Playing && currentReport == null) {
        viewModel.pauseGame()
    }

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
                .blur(if (currentReport != null) 8.dp else 0.dp),
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
                .padding(horizontal = (24.dp * scaleFactor).coerceAtMost(32.dp))
                .blur(if (currentReport != null) 20.dp else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Updated Header
            DetectiveHeader(
                title = stringResource(R.string.case_file_level, currentLevel),
                subtitle = stringResource(R.string.score_text, score),
                scaleFactor = scaleFactor
            )
            
            Spacer(modifier = Modifier.height(12.dp * scaleFactor))

            // Modular Stats Dashboard
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(20.dp * scaleFactor),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp * scaleFactor).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetectiveStatItem(label = stringResource(R.string.label_lives), value = "x$remainingAttempts", color = ErrorRed, scaleFactor = scaleFactor)
                    VerticalDivider(modifier = Modifier.height(24.dp * scaleFactor).width(1.dp), color = Color.White.copy(alpha = 0.1f))
                    DetectiveStatItem(label = stringResource(R.string.label_time), value = String.format("%02d:%02d", remainingTime / 60, remainingTime % 60), color = PrimaryCyan, scaleFactor = scaleFactor)
                    VerticalDivider(modifier = Modifier.height(24.dp * scaleFactor).width(1.dp), color = Color.White.copy(alpha = 0.1f))
                    DetectiveStatItem(
                        label = stringResource(R.string.label_trials), 
                        value = trialHints.size.toString(), 
                        color = SuccessGreen,
                        scaleFactor = scaleFactor,
                        onClick = { 
                            if (!isPaused) {
                                viewModel.recordArchiveOpen()
                                showHistorySheet = true 
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp * scaleFactor))

            Box(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .blur(if (countdownValue != null) 20.dp else 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 0.7f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                        Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryCyan.copy(alpha = alpha), modifier = Modifier.size(18.dp * scaleFactor))
                        Spacer(modifier = Modifier.width(8.dp * scaleFactor))
                        Text(
                            text = stringResource(R.string.msg_analysis_active),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = (11 * scaleFactor).coerceAtMost(16f).sp,
                                fontFamily = Montserrat
                            ),
                            color = PrimaryCyan.copy(alpha = alpha),
                            letterSpacing = (2 * scaleFactor).sp
                        )
                    }

                    val evidenceListState = rememberLazyListState()
                    LaunchedEffect(evidenceHints.size) {
                        if (evidenceHints.isNotEmpty()) {
                            evidenceListState.animateScrollToItem(evidenceHints.size - 1)
                        }
                    }

                    LazyColumn(
                        state = evidenceListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(if (countdownValue != null) 20.dp else 0.dp)
                            .verticalFadingEdge(),
                        verticalArrangement = Arrangement.spacedBy(10.dp * scaleFactor),
                        contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp)
                    ) {
                        itemsIndexed(evidenceHints) { index, hint ->
                            DetectiveHintCard(
                                hint = hint, 
                                isHelperModeEnabled = isHelperModeEnabled, 
                                scaleFactor = scaleFactor,
                                label = stringResource(R.string.log_analysis_number, index + 1),
                                labelColor = PrimaryCyan.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Countdown Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = countdownValue != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

            // Number Picker Area
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp * scaleFactor),
                horizontalArrangement = Arrangement.spacedBy(12.dp * scaleFactor, Alignment.CenterHorizontally),
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

            // Modular Guess Button
            DetectiveButton(
                text = stringResource(R.string.submit_button),
                isPrimary = true,
                enabled = !isPaused,
                scaleFactor = scaleFactor,
                onClick = {
                    val guess = pickerValues.joinToString("")
                    if (guess.length != expectedLength) return@DetectiveButton
                    val result = viewModel.makeGuess(guess)
                    if (result != GuessResult.Invalid) {
                        pickerValues = List(expectedLength) { 0 }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp * scaleFactor))
        }

        // Case Archive Sheet
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
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_case_archive).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = Montserrat,
                            fontSize = (18 * scaleFactor).coerceAtMost(28f).sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (2 * scaleFactor).sp
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalFadingEdge(),
                            verticalArrangement = Arrangement.spacedBy(10.dp * scaleFactor),
                            contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp)
                        ) {
                            itemsIndexed(trialHints) { index, hint -> 
                                DetectiveHintCard(
                                    hint = hint, 
                                    isHelperModeEnabled = isHelperModeEnabled, 
                                    scaleFactor = scaleFactor,
                                    label = stringResource(R.string.log_interrogation_number, index + 1),
                                    labelColor = PrimaryCyan.copy(alpha = 0.7f),
                                    isInterrogation = true
                                )
                            }
                        }
                    }
                }
            }
        }

        // Field Report Overlay (Separated Component)
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NumberVaultPicker(value: Int, scaleFactor: Float, onValueChange: (Int) -> Unit) {
    val pageCount = 10000
    val startIndex = 5000 - (5000 % 10) + value
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = startIndex,
        pageCount = { pageCount }
    )

    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { page -> currentOnValueChange(page % 10) }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove) }
    }

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
        pagerSnapDistance = androidx.compose.foundation.pager.PagerSnapDistance.atMost(4)
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

        val rotationX = pageOffset * -30f
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
