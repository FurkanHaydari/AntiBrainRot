package com.brainfocus.numberdetective.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.toColorInt
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.data.storage.DiagnosticReport
import com.brainfocus.numberdetective.data.storage.SyncLevel
import java.io.File
import java.io.FileOutputStream

object ShareImageGenerator {
    fun generateShareImage(context: Context, isWin: Boolean, score: Int, report: DiagnosticReport?): Uri? {
        try {
            // Load original background
            val options = BitmapFactory.Options()
            options.inMutable = true
            // Using a sample config like ARGB_8888 will consume more memory but is crisp.
            val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.detective_bg, options)
            
            // If the bitmap is extremely large, it's safer to ensure it scales gracefully if needed, 
            // but for a share image, native size usually works well.
            val backgroundBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            
            val canvas = Canvas(backgroundBitmap)
            
            // Emulate the cinematic gradient/overlay from ResultScreen
            val overlayPaint = Paint().apply {
                color = Color.BLACK
                alpha = if (isWin) 120 else 160 // Reduced opacity to make background pop
            }
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), overlayPaint)
            
            // Retrieve Fonts
            val typefaceBold = ResourcesCompat.getFont(context, R.font.montserrat_bold)
            ResourcesCompat.getFont(context, R.font.poppins_regular)
            
            val centerX = canvas.width / 2f
            var startY = canvas.height * 0.4f

            // Title Layer
            val titleStr = context.getString(if (isWin) R.string.mission_accomplished else R.string.mission_failed).uppercase()
            val primaryColor = if (isWin) "#00E5FF".toColorInt() else "#FF3B30".toColorInt()
            
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = primaryColor
                textSize = canvas.width * 0.08f // Dynamic scaling based on bitmap dimensions
                typeface = typefaceBold
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(titleStr, centerX, startY, titlePaint)
            
            // Spacing
            startY += canvas.height * 0.16f

            // Large Score Value
            val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = canvas.width * 0.18f
                typeface = typefaceBold
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(score.toString(), centerX, startY, scorePaint)
            
            // --- Diagnostic Report Section ---
            if (report != null) {
                startY += canvas.height * 0.08f
                drawDiagnosticSection(context, canvas, report, centerX, startY, typefaceBold, isWin)
            }
            
            // Footer App Name
            val appLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = "#7A7A7A".toColorInt()
                textSize = canvas.width * 0.035f
                typeface = typefaceBold
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("NUMBER DETECTIVE", centerX, canvas.height * 0.9f, appLabelPaint)

            // Save payload to a file
            val shareCacheDir = File(context.cacheDir, "share")
            if (!shareCacheDir.exists()) {
                shareCacheDir.mkdirs()
            }
            
            val imageFile = File(shareCacheDir, "mission_result.jpg")
            FileOutputStream(imageFile).use { out ->
                backgroundBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            // Retrieve secure Uri via FileProvider
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun drawDiagnosticSection(
        context: Context,
        canvas: Canvas,
        report: DiagnosticReport,
        centerX: Float,
        startY: Float,
        typeface: android.graphics.Typeface?,
        isWin: Boolean
    ) {
        val rowSpacing = canvas.height * 0.045f
        val barWidth = canvas.width * 0.015f
        val barHeight = canvas.height * 0.006f
        val barGap = canvas.width * 0.005f
        
        val metrics = listOf(
            context.getString(R.string.eval_precision_label) to report.precision.numeric,
            context.getString(R.string.eval_velocity_label) to report.velocity.numeric,
            context.getString(R.string.eval_stability_label) to report.stability.numeric,
            context.getString(R.string.eval_intuition_label) to report.intuition.numeric,
            context.getString(R.string.eval_convergence_label) to report.convergence.numeric
        )

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#B0B0B0".toColorInt()
            textSize = canvas.width * 0.03f
            this.typeface = typeface
            textAlign = Paint.Align.RIGHT
        }

        val barInactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 20
        }

        val barActivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isWin) "#4CAF50".toColorInt() else "#00E5FF".toColorInt()
        }

        metrics.forEachIndexed { index, (label, value) ->
            val rowY = startY + (index * rowSpacing)
            
            // Draw Label (Left of center)
            canvas.drawText(label.uppercase(), centerX - (canvas.width * 0.05f), rowY, labelPaint)
            
            // Draw Bars (Right of center)
            val barsStartX = centerX + (canvas.width * 0.02f)
            repeat(5) { i ->
                val isActive = i < value
                val paint = if (isActive) barActivePaint else barInactivePaint
                
                // Color override for low levels if not win
                if (isActive && !isWin && value <= 2) {
                    paint.color = "#F44336".toColorInt()
                }

                val left = barsStartX + (i * (barWidth + barGap))
                val top = rowY - (barHeight * 1.5f)
                val right = left + barWidth
                val bottom = top + barHeight
                
                canvas.drawRect(left, top, right, bottom, paint)
            }
        }
        
        // Final Sync Label
        val syncLabel = context.getString(R.string.eval_conclusion_label).uppercase()
        val syncPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isWin) "#4CAF50".toColorInt() else "#00E5FF".toColorInt()
            textSize = canvas.width * 0.04f
            this.typeface = typeface
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(syncLabel, centerX, startY + (metrics.size * rowSpacing) + (canvas.height * 0.04f), syncPaint)
    }
}
