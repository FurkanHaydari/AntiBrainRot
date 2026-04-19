package com.brainfocus.numberdetective.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.brainfocus.numberdetective.R
import java.io.File
import java.io.FileOutputStream

object ShareImageGenerator {
    fun generateShareImage(context: Context, isWin: Boolean, score: Int): Uri? {
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
            val primaryColor = if (isWin) Color.parseColor("#00E5FF") else Color.parseColor("#FF3B30")
            
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
            
            // Footer App Name
            val appLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#7A7A7A")
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
}
