package com.cvtracker.vmd.master

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.DisplayItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.net.URLEncoder

object IntentHelper {

    fun startPhoneActivity(activity: Activity, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(
                activity.findViewById(android.R.id.content),
                activity.getString(R.string.no_app_activity_found),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    fun startMapsActivity(activity: Activity, address: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("geo:0,0?q=${URLEncoder.encode(address, "utf-8")}")
        }
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(
                activity.findViewById(android.R.id.content),
                activity.getString(R.string.no_app_activity_found),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}