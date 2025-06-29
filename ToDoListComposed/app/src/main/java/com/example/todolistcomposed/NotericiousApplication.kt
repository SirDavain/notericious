package com.example.todolistcomposed

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotericiousApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}