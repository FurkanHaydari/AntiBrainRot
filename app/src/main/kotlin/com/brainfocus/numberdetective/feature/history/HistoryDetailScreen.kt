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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.brainfocus.numberdetective.data.model.Hint
import com.brainfocus.numberdetective.data.model.HintResolver
import com.brainfocus.numberdetective.data.storage.SyncLevel
import java.util.Locale
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.feature.result.DiagnosticEngine
import com.brainfocus.numberdetective.core.designsystem.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

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
                    fontFamily = Montserrat
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
                        0 -> BriefingTabContent(session)
                        1 -> ArchiveTabContent(session, scaleFactor)
                    }
                }
            }
        }
    }
}

@Composable
private fun BriefingTabContent(
    session: com.brainfocus.numberdetective.data.storage.GameSession
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val verticalScale = (maxHeight.value / 720f).coerceIn(1.0f, 2.5f)
        val isTablet = this@BoxWithConstraints.maxWidth > 600.dp || maxHeight > 600.dp
        val maxContentWidth = if (isTablet) (750.dp * verticalScale).coerceAtMost(this@BoxWithConstraints.maxWidth) 
                             else (500.dp * verticalScale).coerceAtMost(this@BoxWithConstraints.maxWidth)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Digital Intelligence HUD (Transparent Shell)
            Box(
                modifier = Modifier
                    .widthIn(max = maxContentWidth)
                    .fillMaxWidth(if (isTablet) 0.95f else 0.9f)
                    .fillMaxHeight(0.92f)
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(16.dp * verticalScale))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(16.dp * verticalScale)
                    )
                    .padding(horizontal = 24.dp * verticalScale, vertical = 12.dp * verticalScale)
            ) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                // Cognitive diagnostic content
                    
                    Text(
                        text = "INTELLIGENCE FEED: #${session.id.take(12).uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = Montserrat,
                            fontSize = (10 * verticalScale).sp,
                            letterSpacing = (1.5 * verticalScale).sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp * verticalScale))

                    val report = session.diagnosticReport ?: DiagnosticEngine.generateReport(session)
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val coroutineScope = rememberCoroutineScope()
                    val locale = LocalConfiguration.current.locales[0]
                    val formattedTime = "%02d:%02d".format(locale, session.levels.sumOf { it.durationSeconds } / 60, session.levels.sumOf { it.durationSeconds } % 60)

                    // Resolve strings at Composable level to avoid LocalContextGetResourceValueCall warnings
                    val shareTitle = stringResource(R.string.share_score_title)
                    val score = session.totalScore
                    val attempts = session.levels.sumOf { level -> level.hints.count { !it.isSystemHint } }
                    val baseMessage = pluralStringResource(R.plurals.share_score_message, attempts, score, attempts, formattedTime)
    
                    val syncLevelStr = when(report.syncLevel) {
                        SyncLevel.OPTIMAL -> stringResource(R.string.sia_sync_optimal)
                        SyncLevel.STABLE -> stringResource(R.string.sia_sync_stable)
                        SyncLevel.STANDARD -> stringResource(R.string.sia_sync_standard)
                        SyncLevel.SUBOPTIMAL -> stringResource(R.string.sia_sync_suboptimal)
                        SyncLevel.CRITICAL -> stringResource(R.string.sia_sync_critical)
                    }

                    com.brainfocus.numberdetective.feature.result.components.CognitiveDiagnosticReport(
                        report = report,
                        isWin = session.isWin,
                        scaleFactor = verticalScale * 1.1f, // Büyütülmüş puntolar
                        staggered = false,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp * verticalScale))
                    
                    DetectiveButton(
                        text = stringResource(R.string.share_button),
                        isPrimary = false,
                        scaleFactor = verticalScale,
                        onClick = {
                             val playStoreLink = "https://play.google.com/store/apps/details?id=${context.packageName}"
                             val shareMessage = "$baseMessage\n\n$syncLevelStr\n\n$playStoreLink"
                             
                             coroutineScope.launch(Dispatchers.IO) {
                                 val imageUri = com.brainfocus.numberdetective.core.utils.ShareImageGenerator.generateShareImage(context, session.isWin, score, report)
                                 
                                 val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                     if (imageUri != null) {
                                         type = "image/jpeg"
                                         putExtra(android.content.Intent.EXTRA_STREAM, imageUri)
                                         addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                     } else {
                                         type = "text/plain"
                                     }
                                     putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                                 }
                                 
                                 withContext(Dispatchers.Main) {
                                     context.startActivity(android.content.Intent.createChooser(shareIntent, shareTitle))
                                 }
                             }
                        }
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
        session.levels.forEach { levelResult ->
            item {
                DetectiveHeader(
                    title = stringResource(R.string.case_file_level, levelResult.levelNumber),
                    subtitle = stringResource(R.string.correct_answer_label) + ": ${levelResult.secretNumber}",
                    scaleFactor = scaleFactor,
                    rightContent = {
                        Text(
                            text = stringResource(R.string.score_points, levelResult.scoreGained),
                            modifier = Modifier.padding(end = 1.dp * scaleFactor),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                fontSize = (18 * scaleFactor).coerceAtMost(26f).sp
                            )
                        )
                    }
                )
            }
            
            items(levelResult.hints.size) { globalIndex ->
                val hint = levelResult.hints[globalIndex]
                val context = androidx.compose.ui.platform.LocalContext.current
                
                val label = HintResolver.getActionLabel(
                    hint = hint,
                    index = globalIndex,
                    allHints = levelResult.hints,
                    context = context
                )

                val isInterrogation = !hint.isSystemHint || hint.descriptionRes == R.string.log_analysis_success

                DetectiveHintCard(
                    hint = hint,
                    isHelperModeEnabled = true,
                    scaleFactor = scaleFactor,
                    label = label,
                    labelColor = if (hint.descriptionRes == R.string.log_analysis_success) SuccessGreen
                                 else if (isInterrogation) PrimaryCyan 
                                 else TextSecondary.copy(alpha = 0.6f),
                    isInterrogation = isInterrogation,
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
