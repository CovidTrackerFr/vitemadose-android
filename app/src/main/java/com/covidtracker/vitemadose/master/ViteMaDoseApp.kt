package com.covidtracker.vitemadose.master

import android.app.Application
import timber.log.Timber

class ViteMaDoseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Timber.plant(Timber.DebugTree());
    }

    companion object {

        private lateinit var instance: ViteMaDoseApp

        fun get() = instance
    }
}