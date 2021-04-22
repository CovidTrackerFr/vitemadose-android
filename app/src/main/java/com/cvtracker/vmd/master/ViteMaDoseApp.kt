package com.cvtracker.vmd.master

import android.app.Application
import com.cvtracker.vmd.analytics.AnalyticsHelperImpl
import timber.log.Timber

class ViteMaDoseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        Timber.plant(Timber.DebugTree())
        AnalyticsHelperImpl.initApp(this)
    }

    companion object {
        private lateinit var instance: ViteMaDoseApp
        fun get() = instance
    }
}