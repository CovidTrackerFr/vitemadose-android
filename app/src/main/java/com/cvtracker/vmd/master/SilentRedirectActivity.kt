package com.cvtracker.vmd.master

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.home.MainPresenter

/**
 * Not shown activity which take care of silent actions
 */
class SilentRedirectActivity : AppCompatActivity() {

    companion object {
        const val DISABLE_NOTIFICATION_LINK = MainPresenter.BASE_URL + "/notifications_off"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent.dataString
        if (data != null) {
            if (data.startsWith(DISABLE_NOTIFICATION_LINK)) {
                val params = data.replace("$DISABLE_NOTIFICATION_LINK/", "").split("/")
                if (params.size >= 2) {
                    val department = params[0]
                    val centerId = params[1]
                    val notificationId = params[2].toIntOrNull()
                    /** Unsubscribe from topic **/
                    FcmHelper.unsubscribeWithDepartmentAndCenterId(department, centerId)
                    /** Cancel  the notification linked to this subscription **/
                    notificationId?.let { NotificationManagerCompat.from(this).cancel(notificationId) }
                    /** Rollback to favorite only **/
                    PrefHelper.updateBookmark(centerId, department, Bookmark.FAVORITE)
                }
            }
        }
        finish()
    }
}