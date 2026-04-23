package com.brainfocus.numberdetective

import android.app.Application
import android.content.Context
import com.brainfocus.numberdetective.core.utils.LocaleHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
@Suppress("unused")
class NumberDetectiveApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }
}