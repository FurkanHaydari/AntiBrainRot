package com.brainfocus.numberdetective.core.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adds a vertical fading edge to any scrollable container.
 */
fun Modifier.verticalFadingEdge(
    topHeight: Dp = 40.dp,
    bottomHeight: Dp = 40.dp
): Modifier = this
    .graphicsLayer { alpha = 0.99f } // Required for DstIn blend mode
    .drawWithContent {
        drawContent()
        
        val topFadePx = topHeight.toPx()
        val bottomFadePx = bottomHeight.toPx()
        
        if (topFadePx > 0) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = topFadePx
                ),
                blendMode = BlendMode.DstIn
            )
        }
        
        if (bottomFadePx > 0) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = size.height - bottomFadePx,
                    endY = size.height
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }
