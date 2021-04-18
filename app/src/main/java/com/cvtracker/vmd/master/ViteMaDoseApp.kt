package com.cvtracker.vmd.master

import android.app.Application
import com.google.firebase.remoteconfig.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import timber.log.Timber

class ViteMaDoseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Timber.plant(Timber.DebugTree())

        loadCacheFirebaseConfig()

        FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
            })
            fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadCacheFirebaseConfig()
                }
            }
        }
    }

    private fun loadCacheFirebaseConfig() {
        FirebaseRemoteConfig.getInstance().apply {
            getString(URL_BASE_KEY).takeIf { it.isNotBlank() }?.let {
                DataManager.URL_BASE = it
                Timber.d("RemoteConfig set URL_BASE = $it")
            }
            getString(PATH_DATA_DEPARTMENT_KEY).takeIf { it.isNotBlank() }?.let {
                DataManager.PATH_DATA_DEPARTMENT = it
                Timber.d("RemoteConfig set PATH_DATA_DEPARTMENT = $it")
            }
            getString(PATH_STATS_KEY).takeIf { it.isNotBlank() }?.let {
                DataManager.PATH_STATS = it
                Timber.d("RemoteConfig set PATH_STATS = $it")
            }
        }
    }

    companion object {

        private lateinit var instance: ViteMaDoseApp

        private const val URL_BASE_KEY = "url_base"
        private const val PATH_DATA_DEPARTMENT_KEY = "path_data_department"
        private const val PATH_STATS_KEY = "path_stats"

        fun get() = instance
    }
}