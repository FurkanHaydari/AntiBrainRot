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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        val scaleFactor = minOf(maxWidth / 360.dp, maxHeight / 640.dp).coerceIn(1f, 1.7f)
        
        val pagerState = rememberPagerState(pageCount = { 7 })
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
                text = stringResource(R.string.tutorial_title).uppercase(),
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
                TutorialCard(page, scaleFactor, maxWidth)
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

            // Navigation Buttons
            val isLastPage = pagerState.currentPage == pagerState.pageCount - 1
            val buttonHeight = (60.dp * scaleFactor).coerceAtMost(90.dp)
            
            Button(
                onClick = {
                    if (isLastPage) {
                        viewModel.completeOnboarding()
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .widthIn(max = (400.dp * scaleFactor).coerceAtMost(maxWidth))
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

        // --- Layer 4: Close Button ---
        IconButton(
            onClick = onFinish,
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
fun TutorialCard(page: Int, scaleFactor: Float, maxWidth: androidx.compose.ui.unit.Dp) {
    val title = when (page) {
        0 -> R.string.tutorial_page1_title
        1 -> R.string.tutorial_page2_title
        2 -> R.string.tutorial_page3_title
        3 -> R.string.tutorial_page4_title
        4 -> R.string.tutorial_page5_title
        5 -> R.string.tutorial_page6_title
        else -> R.string.tutorial_page7_title
    }
    
    val description = when (page) {
        0 -> R.string.tutorial_page1_desc
        1 -> R.string.tutorial_page2_desc
        2 -> R.string.tutorial_page3_desc
        3 -> R.string.tutorial_page4_desc
        4 -> R.string.tutorial_page5_desc
        5 -> R.string.tutorial_page6_desc
        else -> R.string.tutorial_page7_desc
    }

    val icon = when (page) {
        0 -> "🎯"
        1 -> "🔒"
        2 -> "📂"
        3 -> "🎨"
        4 -> "🚀"
        5 -> "🎖️"
        else -> "⚖️"
    }

    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(32.dp * scaleFactor),
        modifier = Modifier
            .widthIn(max = (440.dp * scaleFactor).coerceAtMost(maxWidth))
            .fillMaxWidth()
            .fillMaxHeight(if (scaleFactor > 1.2f) 0.75f else 0.85f) // Adjust height on tablets to be more compact
            .padding(horizontal = 8.dp * scaleFactor),
        border = RowDefaults.CardBorder
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp * scaleFactor, vertical = 24.dp * scaleFactor)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size((100.dp * scaleFactor).coerceAtMost(160.dp))
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .border(2.dp, PrimaryCyan.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = (48 * scaleFactor).coerceAtMost(80f).sp)
            }

            Spacer(modifier = Modifier.height(32.dp * scaleFactor))

            Text(
                text = stringResource(title).uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = (20 * scaleFactor).coerceAtMost(32f).sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (1.5 * scaleFactor).sp
                ),
                color = PrimaryCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp * scaleFactor))

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
