package com.cvtracker.vmd.util

import android.app.Activity
import android.content.IntentSender
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import timber.log.Timber

interface AppUpdateCallback {
    fun onFlexibleUpdateDownloaded(completeAction: () -> Unit)
    fun onFlexibleUpdateDownloading()
    fun onFlexibleUpdatePending()
}

class AppUpdateHelper(private val activity: Activity, private val callback: AppUpdateCallback) {

    companion object {
        const val DAYS_FOR_FLEXIBLE_UPDATE = 3
        const val APP_UPDATE_INTENT_REQUEST_CODE = 162
    }

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)

    private val appUpdateInfoTask = appUpdateManager.appUpdateInfo

    private val listener = object : InstallStateUpdatedListener {

        private var currentStatus = InstallStatus.UNKNOWN

        override fun onStateUpdate(state: InstallState){
            if(currentStatus != state.installStatus()) {
                currentStatus = state.installStatus()
                when (state.installStatus()) {
                    InstallStatus.DOWNLOADING -> callback.onFlexibleUpdateDownloading()
                    InstallStatus.PENDING -> callback.onFlexibleUpdatePending()
                    InstallStatus.DOWNLOADED -> {
                        callback.onFlexibleUpdateDownloaded(completeUpdateAction)
                        appUpdateManager.unregisterListener(this)
                    }
                    else -> {
                        /** Do nothing **/
                    }
                }
            }
        }
    }

    private val completeUpdateAction: () -> Unit = {
        appUpdateManager.completeUpdate()
    }

    fun checkForUpdates() {
        Timber.d("AppUpdate check")

        appUpdateInfoTask.addOnFailureListener {
            Timber.d("AppUpdate check FAILED ${it.message}")
            Timber.e(it)
        }.addOnSuccessListener { appUpdateInfo ->

            // 1° Check for existing downloaded update
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                Timber.d("Update alredady downloaded")
                callback.onFlexibleUpdateDownloaded(completeUpdateAction)
            }

            // 2° Check for the an update available
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                Timber.d("Update available, priority = ${appUpdateInfo.updatePriority()}")

                /* Check the update priority to set up intent parameters */
                val (updateType, stalenessDays) = when (appUpdateInfo.updatePriority()) {
                    5 -> {
                        /* Update fixing critical issue. No user approval needed */
                        Timber.d("IMMEDIAT update")
                        Pair(AppUpdateType.IMMEDIATE, 0)
                    }
                    4 -> {
                        /* Important Update but not critical. Do not wait a few days before showing the popup */
                        Timber.d("quick FLEXIBLE update")
                        Pair(AppUpdateType.FLEXIBLE, 0)
                    }
                    else -> {
                        /* Standard Update. Wait a few days before showing the popup */
                        Timber.d("standard FLEXIBLE update")
                        Pair(AppUpdateType.FLEXIBLE, DAYS_FOR_FLEXIBLE_UPDATE)
                    }
                }

                val clientVersionStalenessDays = (appUpdateInfo.clientVersionStalenessDays() ?: 0)
                Timber.d("clientVersionStalenessDays = $clientVersionStalenessDays")

                // 3° Request the update if conditions are satisfied
                if (clientVersionStalenessDays >= stalenessDays
                        && appUpdateInfo.isUpdateTypeAllowed(updateType)) {

                    if (updateType == AppUpdateType.FLEXIBLE) {
                        appUpdateManager.registerListener(listener)
                    }

                    Timber.d("Request App update")
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                updateType,
                                activity,
                                APP_UPDATE_INTENT_REQUEST_CODE
                        )
                    } catch (e: IntentSender.SendIntentException){
                        /** A very few crashs comes with this exception **/
                        Timber.e(e)
                    }
                }
            }
        }
    }
}