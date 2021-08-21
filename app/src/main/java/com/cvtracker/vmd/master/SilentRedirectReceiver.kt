package com.cvtracker.vmd.master

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.cvtracker.vmd.data.Bookmark

class SilentRedirectReceiver : BroadcastReceiver() {

    companion object {
        private const val EXTRA_DEPARTMENT = "EXTRA_DEPARTMENT"
        private const val EXTRA_CENTER_ID = "EXTRA_CENTER_ID"
        private const val EXTRA_TOPIC = "EXTRA_TOPIC"
        private const val EXTRA_TYPE = "EXTRA_TYPE"
        private const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"

        fun buildIntent(context: Context, department: String, centerId: String, topic: String, type: String, notificationId: Int) =
                Intent(context, SilentRedirectReceiver::class.java)
                        .putExtra(EXTRA_DEPARTMENT, department)
                        .putExtra(EXTRA_CENTER_ID, centerId)
                        .putExtra(EXTRA_TOPIC, topic)
                        .putExtra(EXTRA_TYPE, type)
                        .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val contextNotNull = context ?: return
        intent ?: return

        val department = intent.getStringExtra(EXTRA_DEPARTMENT) ?: return
        val centerId = intent.getStringExtra(EXTRA_CENTER_ID) ?: return
        val topic = intent.getStringExtra(EXTRA_TOPIC) ?: ""
        val type = intent.getStringExtra(EXTRA_TYPE) ?: ""
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        /** Unsubscribe from topic **/
        FcmHelper.unsubscribeFromDepartmentAndCenterId(department, centerId, FcmHelper.isTopicChronodose(topic))

        /** Cancel  the notification linked to this subscription **/
        NotificationManagerCompat.from(contextNotNull).cancel(notificationId)

        /** Rollback to favorite only **/
        PrefHelper.updateBookmark(centerId, department, Bookmark.FAVORITE)
    }
}