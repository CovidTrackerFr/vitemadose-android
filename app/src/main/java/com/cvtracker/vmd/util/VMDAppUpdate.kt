package com.cvtracker.vmd.util

import android.view.View
import com.cvtracker.vmd.R
import com.cvtracker.vmd.extensions.colorAttr
import com.cvtracker.vmd.home.MainActivity
import com.google.android.material.snackbar.Snackbar

class VMDAppUpdate(activity: MainActivity, val anchorView: View) : AppUpdateCallback {

    private val appUpdateHelper = AppUpdateHelper(activity, this)

    fun checkUpdates(){
        appUpdateHelper.checkForUpdates()
    }

    override fun onFlexibleUpdateDownloaded(completeAction: () -> Unit) {
        Snackbar.make(anchorView, R.string.app_update_downloaded, Snackbar.LENGTH_INDEFINITE)
            .setActionTextColor(anchorView.colorAttr(R.attr.colorPrimary))
            .setAction("YES") { completeAction.invoke() }
            .show()
    }

    override fun onFlexibleUpdateDownloading() {
        Snackbar.make(anchorView, R.string.app_update_downloading, Snackbar.LENGTH_LONG).show()
    }

    override fun onFlexibleUpdatePending() {
        Snackbar.make(anchorView, R.string.app_update_pending, Snackbar.LENGTH_SHORT).show()
    }
}