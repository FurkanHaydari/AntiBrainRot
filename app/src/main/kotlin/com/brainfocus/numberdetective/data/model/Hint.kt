package com.brainfocus.numberdetective.data.model

import android.content.Context
import androidx.annotation.StringRes
import com.brainfocus.numberdetective.R

enum class DigitStatus {
    CORRECT_POS, // Right number, right spot (Green)
    WRONG_POS,    // Right number, wrong spot (Yellow)
    INCORRECT     // Incorrect number (Red)
}

data class Hint(
    val guess: String,
    val correct: Int,
    val misplaced: Int,
    val description: String = "",
    @StringRes val descriptionRes: Int? = null,
    val descriptionArgs: List<Any> = emptyList(),
    val digitStatuses: List<DigitStatus>? = null,
    val timestamp: Int? = null,
    val isSystemHint: Boolean = false
) {
    /**
     * İpucu metnini dinamik olarak çözümler.
     * Yeni Öncelik: Dynamic Logic -> SystemHint -> Snapshot (description)
     */
    fun getDisplayText(context: Context, isLevel3: Boolean = false): String {
        // 1. Dinamik Veri Odaklı Metin Üretimi (Data-Driven Translation) - En Yüksek Öncelik (Dil Değişimi Desteği)
        val resId = when {
            isLevel3 -> getLevel3HintRes(correct, misplaced)
            else -> getLevel1HintRes(correct, misplaced)
        }
        
        if (resId != 0) return context.getString(resId)

        // 2. Eğer bir kaynak ID'si (descriptionRes) zaten atanmışsa onu kullan (ACCESS GRANTED vb.)
        if (descriptionRes != null && descriptionRes != 0) {
            return try {
                if (descriptionArgs.isNotEmpty()) {
                    context.getString(descriptionRes, *descriptionArgs.toTypedArray())
                } else {
                    context.getString(descriptionRes)
                }
            } catch (e: Exception) {
                description // Fallback
            }
        }

        // 3. Fallback: Dondurulmuş metni bas
        if (description.isNotBlank()) return description

        return ""
    }

    private fun getLevel1HintRes(correct: Int, misplaced: Int): Int {
        return when {
            correct == 3 -> R.string.hint_all_correct
            correct == 0 && misplaced == 0 -> R.string.hint_none
            correct == 1 && misplaced == 0 -> R.string.hint_1
            correct == 0 && misplaced == 1 -> R.string.hint_2
            correct == 0 && misplaced == 2 -> R.string.hint_3
            correct == 1 && misplaced == 1 -> R.string.hint_5
            else -> 0
        }
    }

    private fun getLevel3HintRes(correct: Int, misplaced: Int): Int {
        return when {
            correct == 4 -> R.string.hint_l3_all_correct
            correct == 0 && misplaced == 0 -> R.string.hint_l3_none
            correct == 1 && misplaced == 0 -> R.string.hint_l3_1
            correct == 0 && misplaced == 1 -> R.string.hint_l3_2
            correct == 1 && misplaced == 1 -> R.string.hint_l3_3
            correct == 2 && misplaced == 0 -> R.string.hint_l3_5
            else -> 0
        }
    }
}
