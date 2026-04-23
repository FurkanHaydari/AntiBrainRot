package com.brainfocus.numberdetective.core.di

import com.brainfocus.numberdetective.game.NumberDetectiveGame
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object GameModule {
    @Provides
    @ViewModelScoped
    fun provideNumberDetectiveGame(): NumberDetectiveGame {
        return NumberDetectiveGame()
    }
}
