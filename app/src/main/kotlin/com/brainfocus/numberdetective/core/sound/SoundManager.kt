package com.brainfocus.numberdetective.core.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.brainfocus.numberdetective.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var soundPool: SoundPool? = null
    private var tickSoundId: Int = 0
    private var winSoundId: Int = 0
    private var wrongSoundId: Int = 0
    private var correctSoundId: Int = 0
    private var buttonClickId: Int = 0
    private var loseSoundId: Int = 0
    private var levelUpSoundId: Int = 0
    private var partialWrongSoundId: Int = 0
    private var beepSoundId: Int = 0
    private var isInitialized = false
    private var isSoundEnabled = true // New flag for global sound control
    private var loadedSounds = 0
    private var totalSounds = 9
    
    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }
    
    fun initialize() {
        if (isInitialized) return
        
        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            soundPool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attributes)
                .build()
            
            soundPool?.setOnLoadCompleteListener { _, _, status ->
                synchronized(this) {
                    if (status == 0) {
                        loadedSounds++
                        if (loadedSounds == totalSounds) {
                            isInitialized = true
                        }
                    }
                }
            }
            
            // Reset counters and IDs
            loadedSounds = 0
            isInitialized = false
            tickSoundId = 0
            winSoundId = 0
            wrongSoundId = 0
            levelUpSoundId = 0
            partialWrongSoundId = 0
            
            tickSoundId = loadSound(R.raw.tick_sound)
            winSoundId = loadSound(R.raw.win_sound)
            wrongSoundId = loadSound(R.raw.wrong_guess)
            correctSoundId = loadSound(R.raw.correct_guess)
            buttonClickId = loadSound(R.raw.button_click)
            loseSoundId = loadSound(R.raw.lose_sound)
            levelUpSoundId = loadSound(R.raw.level_up)
            partialWrongSoundId = loadSound(R.raw.partial_or_wrong_guess)
            beepSoundId = loadSound(R.raw.beep)
            
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error initializing SoundManager", e)
            release()
        }
    }
    
    fun playTickSound() {
        // Keeping for compatibility but delegating to playBeepSound if needed 
        // or just letting it be if user wants both. The user asked to replace tick with beep in timer.
        playBeepSound()
    }

    fun playBeepSound() {
        if (!isSoundEnabled || !isInitialized) return
        if (beepSoundId == 0) return
        try {
            soundPool?.play(beepSoundId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing beep sound", e)
        }
    }
    
    fun playWinSound() {
        if (!isSoundEnabled || !isInitialized) return
        if (winSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid win sound ID")
            return
        }
        try {
            soundPool?.play(winSoundId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing win sound", e)
        }
    }
    
    fun playWrongSound() {
        if (!isSoundEnabled || !isInitialized) return
        if (wrongSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid wrong sound ID")
            return
        }
        try {
            soundPool?.play(wrongSoundId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing wrong sound", e)
        }
    }
    
    fun playCorrectSound() {
        if (!isSoundEnabled || !isInitialized) return
        if (correctSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid correct sound ID")
            return
        }
        try {
            soundPool?.play(correctSoundId, 0.7f, 0.7f, 1, 0, 1f)
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing correct sound", e)
        }
    }

    fun playButtonClick() {
        if (!isSoundEnabled || !isInitialized) return
        if (buttonClickId == 0) {
            android.util.Log.e("SoundManager", "Invalid button click sound ID")
            return
        }
        try {
            soundPool?.play(buttonClickId, 0.5f, 0.5f, 1, 0, 1f)
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing button click sound", e)
        }
    }

    fun playLoseSound() {
        if (!isSoundEnabled || !isInitialized) return
        if (loseSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid lose sound ID")
            return
        }
        try {
            soundPool?.play(loseSoundId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing lose sound", e)
        }
    }

    fun playLevelUpSound() {
        if (!isSoundEnabled || !isInitialized) return
        if (levelUpSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid level up sound ID")
            return
        }
        try {
            soundPool?.play(levelUpSoundId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing level up sound", e)
        }
    }

    fun playPartialWrongSound() {
        if (!isSoundEnabled || !isInitialized) return
        if (partialWrongSoundId == 0) {
            android.util.Log.e("SoundManager", "Invalid partial/wrong sound ID")
            return
        }
        try {
            soundPool?.play(partialWrongSoundId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error playing partial/wrong sound", e)
        }
    }

    private fun loadSound(resId: Int): Int {
        return try {
            val soundId = soundPool?.load(context, resId, 1) ?: 0
            if (soundId == 0) {
                android.util.Log.e("SoundManager", "Failed to load sound ID: $resId")
            }
            soundId
        } catch (e: android.content.res.Resources.NotFoundException) {
            android.util.Log.e("SoundManager", "Resource not found: $resId")
            0
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error loading sound $resId: ${e.message}")
            0
        }
    }

    fun release() {
        try {
            soundPool?.release()
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error releasing SoundPool", e)
        } finally {
            soundPool = null
            isInitialized = false
            loadedSounds = 0
            tickSoundId = 0
            winSoundId = 0
            wrongSoundId = 0
            correctSoundId = 0
            buttonClickId = 0
            loseSoundId = 0
            levelUpSoundId = 0
            partialWrongSoundId = 0
            beepSoundId = 0
        }
    }
}
