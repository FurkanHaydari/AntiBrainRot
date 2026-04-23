package com.brainfocus.numberdetective.data.model

import android.content.Context
import com.brainfocus.numberdetective.R

/**
 * HintResolver
 * 
 * Bu sınıf, Hint (İpucu) verilerini insan tarafından okunabilir 
 * metinlere ve etiketlere dönüştüren merkezi mantığı barındırır.
 * 
 * SİA Protokolü v4.4: 
 * - Eylem Etiketi (Action Label) ve İpucu Açıklaması (Description) birbirinden ayrılmıştır.
 */
object HintResolver {

    /**
     * İpucunun eylemsel başlığını/etiketini çözer.
     * Örnek: "SORGU #1", "BAŞLANGIÇ İSTİHBARATI", "ERİŞİM ONAYLANDI"
     */
    fun getActionLabel(
        hint: Hint, 
        index: Int, 
        allHints: List<Hint>, 
        context: Context
    ): String {
        // 1. Durum: Tam Başarı (Success)
        if (hint.descriptionRes == R.string.log_analysis_success) {
            return context.getString(R.string.log_analysis_success)
        }

        // 2. Durum: Sistem İpucu (Başlangıç İstihbaratı vb.)
        if (hint.isSystemHint) {
            val intelligenceNumber = allHints.take(index + 1).count { it.isSystemHint }
            return context.getString(R.string.initial_intelligence_number, intelligenceNumber)
        }

        // 3. Durum: Kullanıcı Sorgulaması (Interrogation)
        val interrogationNumber = allHints.take(index + 1).count { 
            !it.isSystemHint && it.descriptionRes != R.string.log_analysis_success 
        }
        return context.getString(R.string.log_interrogation_number, interrogationNumber)
    }

    /**
     * İpucunun mantıksal açıklamasını çözer.
     * Örnek: "1 rakam doğru ve doğru yerde"
     */
    fun getHintDescription(hint: Hint, context: Context): String {
        val isLevel3 = hint.guess.length == 4
        
        // 1. Dinamik Veri Odaklı Metin Üretimi (Data-Driven Translation)
        val resId = if (isLevel3) {
            getLevel3HintRes(hint.correct, hint.misplaced)
        } else {
            getLevel1HintRes(hint.correct, hint.misplaced)
        }
        
        if (resId != 0) {
            val baseDescription = context.getString(resId)
            val totalHits = hint.correct + hint.misplaced
            val threshold = if (isLevel3) 3 else 2

            return when {
                hint.isSystemHint -> baseDescription
                hint.correct == (if (isLevel3) 4 else 3) -> baseDescription
                totalHits >= threshold -> context.getString(R.string.hint_prefix_close, baseDescription)
                else -> context.getString(R.string.hint_prefix_fail, baseDescription)
            }
        }

        // 2. Eğer bir kaynak ID'si (descriptionRes) zaten atanmışsa fallback olarak kullan
        if (hint.descriptionRes != null && hint.descriptionRes != 0) {
            return try {
                if (hint.descriptionArgs.isNotEmpty()) {
                    context.getString(hint.descriptionRes, *hint.descriptionArgs.toTypedArray())
                } else {
                    context.getString(hint.descriptionRes)
                }
            } catch (e: Exception) {
                hint.description
            }
        }

        // 3. Nihai Fallback: Dondurulmuş metni bas
        return hint.description
    }

    private fun getLevel1HintRes(correct: Int, misplaced: Int): Int {
        return when {
            correct == 3 -> R.string.hint_all_correct
            correct == 0 && misplaced == 0 -> R.string.hint_none
            correct == 1 && misplaced == 0 -> R.string.hint_1
            correct == 0 && misplaced == 1 -> R.string.hint_2
            correct == 0 && misplaced == 2 -> R.string.hint_3
            correct == 1 && misplaced == 1 -> R.string.hint_5
            correct == 2 && misplaced == 0 -> R.string.hint_6
            correct == 0 && misplaced == 3 -> R.string.hint_7
            correct == 1 && misplaced == 2 -> R.string.hint_8
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
            correct == 0 && misplaced == 2 -> R.string.hint_l3_6
            correct == 3 && misplaced == 0 -> R.string.hint_l3_7
            correct == 0 && misplaced == 3 -> R.string.hint_l3_8
            correct == 1 && misplaced == 2 -> R.string.hint_l3_9
            correct == 2 && misplaced == 1 -> R.string.hint_l3_10
            correct == 0 && misplaced == 4 -> R.string.hint_l3_11
            correct == 1 && misplaced == 3 -> R.string.hint_l3_12
            correct == 2 && misplaced == 2 -> R.string.hint_l3_13
            else -> 0
        }
    }
}
