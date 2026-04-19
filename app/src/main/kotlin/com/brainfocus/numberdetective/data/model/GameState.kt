package com.brainfocus.numberdetective.data.model

sealed class GameState {
    object Initial : GameState()
    object Playing : GameState()
    data class Win(val score: Int) : GameState()
    data class GameOver(val score: Int) : GameState()
    data class Error(val message: String) : GameState()
}
