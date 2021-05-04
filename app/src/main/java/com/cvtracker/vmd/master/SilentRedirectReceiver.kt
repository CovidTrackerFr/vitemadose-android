package com.cvtracker.vmd.master

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.home.MainPresenter

class SilentRedirectReceiver : BroadcastReceiver() {

    companion object {
        const val DISABLE_NOTIFICATION_LINK = MainPresenter.BASE_URL + "/notifications_off"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val contextNotNull = context ?: return
        val data = intent?.dataString ?: return
        if (data.startsWith(DISABLE_NOTIFICATION_LINK)) {
            val params = data.replace("$DISABLE_NOTIFICATION_LINK/", "").split("/")
            if (params.size >= 3) {
                val department = params[0]
                val centerId = params[1]
                val notificationId = params[2].toIntOrNull()
                /** Unsubscribe from topic **/
                FcmHelper.unsubscribeWithDepartmentAndCenterId(department, centerId)
                /** Cancel  the notification linked to this subscription **/
                notificationId?.let { NotificationManagerCompat.from(contextNotNull).cancel(notificationId) }
                /** Rollback to favorite only **/
                PrefHelper.updateBookmark(centerId, department, Bookmark.FAVORITE)
            }
        }
    }
}