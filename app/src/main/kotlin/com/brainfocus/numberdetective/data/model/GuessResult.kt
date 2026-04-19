package com.brainfocus.numberdetective.data.model

sealed class GuessResult {
    object Correct : GuessResult()
    object Wrong : GuessResult()
    object Invalid : GuessResult()  // Eklenen yeni case
    data class Partial(val correctDigits: Int, val wrongPositionDigits: Int) : GuessResult()
}