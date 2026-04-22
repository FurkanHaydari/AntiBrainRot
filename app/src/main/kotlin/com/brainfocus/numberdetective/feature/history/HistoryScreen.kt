package com.brainfocus.numberdetective.feature.history

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import com.brainfocus.numberdetective.data.storage.GameSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.text.font.FontFamily

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val history by viewModel.history.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

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
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Black.copy(alpha = 0.95f))
                    )
                )
        )

        // --- Layer 2: Main Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Header
            DetectiveHeader(
                title = stringResource(R.string.label_tab_archive),
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

            Spacer(modifier = Modifier.height(16.dp * scaleFactor))

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.msg_no_evidence_waiting),
                        color = TextSecondary.copy(alpha = 0.5f),
                        letterSpacing = (1 * scaleFactor).sp,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = (16 * scaleFactor).coerceAtMost(24f).sp
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp * scaleFactor),
                    contentPadding = PaddingValues(bottom = 24.dp * scaleFactor),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    itemsIndexed(
                        items = history,
                        key = { index, session -> session.id }
                    ) { index, session ->
                        val caseNumber = history.size - index
                        HistoryItem(
                            session = session, 
                            caseNumber = caseNumber,
                            scaleFactor = scaleFactor,
                            maxWidth = maxWidthDp,
                            onClick = { onNavigateToDetail(session.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(session: GameSession, caseNumber: Int, scaleFactor: Float, maxWidth: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateString = remember(session.timestamp) { dateFormat.format(Date(session.timestamp)) }
    
    Surface(
        onClick = onClick,
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp * scaleFactor),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier
            .widthIn(max = (550.dp * scaleFactor).coerceAtMost(maxWidth))
            .fillMaxWidth()
    ) {
        Box {
            // Paperclip Effect from DetectiveComponents
            PaperClip(
                scaleFactor = scaleFactor,
                modifier = Modifier
                    .offset(y = (-4).dp * scaleFactor)
                    .padding(start = 12.dp * scaleFactor)
                    .align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .padding(20.dp * scaleFactor)
                    .fillMaxWidth()
            ) {
                // Header: Case # and Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = PrimaryCyan.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp * scaleFactor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "CASE #$caseNumber",
                            modifier = Modifier.padding(horizontal = 8.dp * scaleFactor, vertical = 2.dp * scaleFactor),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (14 * scaleFactor).coerceAtMost(20f).sp,
                                fontFamily = Montserrat
                            ),
                            color = PrimaryCyan
                        )
                    }
                    
                    MissionStamp(
                        text = if (session.isWin) stringResource(R.string.mission_accomplished) 
                               else stringResource(R.string.mission_failed),
                        color = if (session.isWin) SuccessGreen else ErrorRed,
                        scaleFactor = scaleFactor * 0.8f
                    )
                }

                Spacer(modifier = Modifier.height(10.dp * scaleFactor))

                // Date and Session Details
                Text(
                    text = dateString.uppercase(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (12 * scaleFactor).coerceAtMost(20f).sp,
                        fontFamily = Montserrat,
                        letterSpacing = (2 * scaleFactor).sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp * scaleFactor))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.final_score).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = (12 * scaleFactor).coerceAtMost(18f).sp,
                                letterSpacing = (1.5 * scaleFactor).sp
                            ),
                            color = TextSecondary.copy(alpha = 0.4f)
                        )
                        Text(
                            text = session.totalScore.toString(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (36 * scaleFactor).coerceAtMost(52f).sp
                            ),
                            color = PrimaryCyan
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp * scaleFactor),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp * scaleFactor)
                    ) {
                        session.levels.forEach { level ->
                            LevelBadge(levelNumber = level.levelNumber, scaleFactor = scaleFactor * 0.9f)
                        }
                    }
                }
            }
        }
    }
}
