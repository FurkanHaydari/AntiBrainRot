package com.brainfocus.numberdetective.data.model

sealed class GuessResult {
    object Correct : GuessResult()
    object Wrong : GuessResult()
    object Invalid : GuessResult()  // Eklenen yeni case
    object Partial : GuessResult()
}