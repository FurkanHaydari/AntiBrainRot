package com.brainfocus.numberdetective

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.brainfocus.numberdetective.feature.AppNavigation
import com.brainfocus.numberdetective.core.designsystem.NumberDetectiveTheme
import com.brainfocus.numberdetective.core.utils.LocaleHelper
import com.brainfocus.numberdetective.core.sound.SoundManager
import com.brainfocus.numberdetective.data.storage.DataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var soundManager: SoundManager

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize SoundManager and sync with settings
        soundManager.initialize()
        lifecycleScope.launch {
            dataStoreManager.isSoundEnabledFlow.collect { enabled ->
                soundManager.setSoundEnabled(enabled)
            }
        }

        setContent {
            val currentLanguage = remember { mutableStateOf(LocaleHelper.getLanguage(this)) }
            
            NumberDetectiveTheme {
                AppNavigation(
                    onPlayClick = {
                        playButtonClickSound()
                    },
                    onLanguageChange = { lang ->
                        LocaleHelper.setLocale(this@MainActivity, lang)
                        currentLanguage.value = lang
                        recreate() // Recreate activity to apply new language across the app
                    },
                    currentLanguage = currentLanguage.value
                )
            }
        }
    }

    private fun playButtonClickSound() {
        soundManager.playButtonClick()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
