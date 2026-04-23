package com.brainfocus.numberdetective.data.model

sealed class GameState {
    object Initial : GameState()
    object Playing : GameState()
    object Win : GameState()
    object GameOver : GameState()
    object Error : GameState()
}
