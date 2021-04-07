package com.covidtracker.vitemadose.master

import android.app.Application

class ViteMaDoseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {

        private lateinit var instance: ViteMaDoseApp

        fun get() = instance
    }
}