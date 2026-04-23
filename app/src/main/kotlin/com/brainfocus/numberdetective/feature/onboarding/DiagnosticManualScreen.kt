package com.brainfocus.numberdetective.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import kotlinx.coroutines.launch

@Composable
fun DiagnosticManualScreen(
    onDismiss: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        val scaleFactor = minOf(maxWidth / 360.dp, maxHeight / 640.dp).coerceIn(1f, 1.7f)
        
        // 6 pages: Intro + 5 Pillars
        val pagerState = rememberPagerState(pageCount = { 6 })
        val scope = rememberCoroutineScope()

        // --- Layer 1: Atmospheric Background ---
        Image(
            painter = painterResource(id = R.drawable.detective_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // --- Layer 2: Deep Gradient Overlay ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundOverlayGradient)
        )

        // --- Layer 3: Pager Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = (24.dp * scaleFactor).coerceAtMost(80.dp))
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.manual_title).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = (12 * scaleFactor).coerceAtMost(20f).sp,
                    letterSpacing = (4 * scaleFactor).sp
                ),
                color = PrimaryCyan.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 16.dp * scaleFactor)
            )

            Spacer(modifier = Modifier.height(32.dp * scaleFactor))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                DiagnosticCard(page, scaleFactor, maxWidth)
            }

            Spacer(modifier = Modifier.height(24.dp * scaleFactor))

            // Page Indicators
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(bottom = 24.dp * scaleFactor),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) PrimaryCyan else Color.White.copy(alpha = 0.2f)
                    val width = if (pagerState.currentPage == iteration) 24.dp * scaleFactor else 8.dp * scaleFactor
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp * scaleFactor)
                            .clip(CircleShape)
                            .background(color)
                            .width(width)
                            .height(8.dp * scaleFactor)
                            .animateContentSize()
                    )
                }
            }

            // Navigation Button
            val isLastPage = pagerState.currentPage == pagerState.pageCount - 1
            val buttonHeight = (60.dp * scaleFactor).coerceAtMost(90.dp)
            
            Button(
                onClick = {
                    if (isLastPage) {
                        onDismiss()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .widthIn(max = if (maxWidth > 600.dp) 800.dp else 400.dp * scaleFactor)
                    .fillMaxWidth()
                    .height(buttonHeight)
                    .clip(RoundedCornerShape(20.dp * scaleFactor)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isLastPage) PlayButtonGradient else androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.1f))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLastPage) stringResource(R.string.tutorial_finish_button).uppercase() else stringResource(R.string.tutorial_continue_button).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = (16 * scaleFactor).coerceAtMost(24f).sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp * scaleFactor))
        }

        // Close Button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp * scaleFactor, end = 16.dp * scaleFactor)
                .statusBarsPadding()
                .size(44.dp * scaleFactor)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, PrimaryCyan.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Manual",
                tint = PrimaryCyan.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp * scaleFactor)
            )
        }
    }
}

@Composable
fun DiagnosticCard(page: Int, scaleFactor: Float, maxWidth: androidx.compose.ui.unit.Dp) {
    val title = when (page) {
        0 -> R.string.manual_intro_title
        1 -> R.string.manual_precision_title
        2 -> R.string.manual_velocity_title
        3 -> R.string.manual_stability_title
        4 -> R.string.manual_intuition_title
        else -> R.string.manual_convergence_title
    }
    
    val description = when (page) {
        0 -> R.string.manual_intro_desc
        1 -> R.string.manual_precision_desc
        2 -> R.string.manual_velocity_desc
        3 -> R.string.manual_stability_desc
        4 -> R.string.manual_intuition_desc
        else -> R.string.manual_convergence_desc
    }

    val icon = when (page) {
        0 -> "🖥️"
        1 -> "🎯"
        2 -> "⚡"
        3 -> "🧘"
        4 -> "👁️"
        else -> "🔭"
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(32.dp * scaleFactor),
        modifier = Modifier
            .widthIn(max = if (maxWidth > 600.dp) 800.dp else 440.dp * scaleFactor)
            .fillMaxWidth()
            .heightIn(max = if (scaleFactor > 1.2f) screenHeight * 0.88f else screenHeight * 0.95f) // Dynamic height with a max cap
            .padding(horizontal = 8.dp * scaleFactor),
        border = RowDefaults.CardBorder
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp * scaleFactor, vertical = 20.dp * scaleFactor)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Changed from Center to Top for better use of space
        ) {
            Spacer(modifier = Modifier.height(16.dp * scaleFactor)) // Small top margin

            Box(
                modifier = Modifier
                    .size((80.dp * scaleFactor).coerceAtMost(120.dp)) // Slightly smaller icon
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .border(2.dp, PrimaryCyan.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = (40 * scaleFactor).coerceAtMost(64f).sp)
            }

            Spacer(modifier = Modifier.height(24.dp * scaleFactor)) // Reduced from 32dp

            Text(
                text = stringResource(title).uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = (18 * scaleFactor).coerceAtMost(28f).sp, // Slightly smaller title
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (1.5 * scaleFactor).sp
                ),
                color = PrimaryCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp * scaleFactor)) // Reduced margin

            Text(
                text = stringResource(description),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = (15 * scaleFactor).coerceAtMost(24f).sp,
                    lineHeight = (24 * scaleFactor).coerceAtMost(34f).sp
                ),
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}
