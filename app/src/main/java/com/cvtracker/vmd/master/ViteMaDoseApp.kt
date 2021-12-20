package com.cvtracker.vmd.master

import android.app.Application
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.Disclaimer
import com.cvtracker.vmd.data.DisclaimerSeverity
import com.cvtracker.vmd.home.MainPresenter
import com.google.firebase.remoteconfig.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import timber.log.Timber
import java.util.*

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

        migrateNotificationsIfNeeded()
    }

    private fun migrateNotificationsIfNeeded() {
        val notificationsBookmark = PrefHelper.centersBookmark.filter {
            it.bookmark == Bookmark.NOTIFICATION_CHRONODOSE
        }
        notificationsBookmark.forEach {
            // unsubscribe from chronodose firebase notification
            FcmHelper.unsubscribeFromDepartmentAndCenterId(it.department, it.centerId, true)
            // update the local pref
            PrefHelper.updateBookmark(it.centerId, it.department, Bookmark.NOTIFICATION)
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
            getLong(CHRONODOSE_MIN_COUNT_KEY).takeIf { it >= 1 }?.let {
                CHRONODOSE_MIN_COUNT = it.toInt()
                Timber.d("RemoteConfig set CHRONODOSE_MIN_COUNT = $it")
            }
            setUpDisclaimerFromConfig(this)
        }
    }

    private fun setUpDisclaimerFromConfig(firebaseRemoteConfig: FirebaseRemoteConfig) {
        firebaseRemoteConfig.apply {
            val disclaimerEnabled = getBoolean(DISCLAIMER_ENABLED_KEY)
            val disclaimerMessage = getString(DISCLAIMER_MESSAGE_KEY)
            val disclaimerRepeatDays = getLong(DISCLAIMER_REPEAT_KEY)
            if(disclaimerEnabled && disclaimerMessage.isNotBlank()){
                val severity = try {
                    DisclaimerSeverity.valueOf(getString(DISCLAIMER_SEVERITY_KEY).toUpperCase(Locale.getDefault()))
                }catch (e: IllegalArgumentException){
                    DisclaimerSeverity.INFO
                }
                val configDisclaimer = Disclaimer(severity, disclaimerMessage, disclaimerRepeatDays)
                val minTimestampToRepeat = Calendar.getInstance().apply {
                    timeInMillis = PrefHelper.lastDisclaimerClosedTimestamp
                    add(Calendar.DAY_OF_YEAR, disclaimerRepeatDays.toInt())
                }.timeInMillis

                MainPresenter.disclaimer = when{
                    configDisclaimer.message != PrefHelper.lastDisclaimerClosedMessage -> {
                        /** Remote Config come with a new message **/
                        Timber.d("RemoteConfig set DisclaimerMessage because of new message = $severity/$disclaimerMessage/$disclaimerRepeatDays")
                        configDisclaimer
                    }
                    minTimestampToRepeat < Date().time -> {
                        /** It's time to repeat latest closed message **/
                        Timber.d("RemoteConfig set DisclaimerMessage because of repeat message = $severity/$disclaimerMessage/$disclaimerRepeatDays")
                        configDisclaimer
                    }
                    else -> null
                }
            }
        }
    }

    companion object {

        var CHRONODOSE_MIN_COUNT = 1

        private lateinit var instance: ViteMaDoseApp

        private const val URL_BASE_KEY = "url_base"
        private const val PATH_DATA_DEPARTMENT_KEY = "path_data_department"
        private const val PATH_STATS_KEY = "path_stats"
        private const val CHRONODOSE_MIN_COUNT_KEY = "chronodose_min_count"
        private const val DISCLAIMER_ENABLED_KEY = "data_disclaimer_enabled"
        private const val DISCLAIMER_MESSAGE_KEY = "data_disclaimer_message"
        private const val DISCLAIMER_SEVERITY_KEY = "data_disclaimer_severity"
        private const val DISCLAIMER_REPEAT_KEY = "data_disclaimer_repeat_days"

        fun get() = instance
    }
}