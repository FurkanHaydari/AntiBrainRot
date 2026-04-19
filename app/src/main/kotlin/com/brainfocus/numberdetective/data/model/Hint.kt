package com.brainfocus.numberdetective.data.model

import androidx.annotation.StringRes

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
    val timestamp: Int? = null
)
