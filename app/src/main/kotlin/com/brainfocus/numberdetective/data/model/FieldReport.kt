package com.brainfocus.numberdetective.data.model

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.brainfocus.numberdetective.R

sealed class FieldReport(
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int = 0,
    @PluralsRes val pluralRes: Int = 0,
    val quantity: Int = 0,
    val isPositive: Boolean,
    val messageArgs: List<Any> = emptyList()
) {
    class Promotion(level: Int, bonusAttempts: Int, bonusTime: Int) : FieldReport(
        titleRes = R.string.report_promotion_title,
        pluralRes = R.plurals.report_promotion_msg,
        quantity = bonusAttempts,
        messageArgs = listOf(level, bonusAttempts, bonusTime),
        isPositive = true
    )

    class Compromised(remaining: Int) : FieldReport(
        titleRes = R.string.report_compromised_title,
        pluralRes = R.plurals.report_compromised_msg,
        quantity = remaining,
        messageArgs = listOf(remaining),
        isPositive = false
    )

    class Validation(@StringRes title: Int, @StringRes message: Int) : FieldReport(
        titleRes = title,
        messageRes = message,
        isPositive = false
    )

    class Pause : FieldReport(
        titleRes = R.string.report_pause_title,
        messageRes = R.string.report_pause_msg,
        isPositive = true
    )
}
