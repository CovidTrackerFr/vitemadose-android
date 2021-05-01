package com.cvtracker.vmd.master

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cvtracker.vmd.R
import com.cvtracker.vmd.bookmark.BookmarkActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class ViteMaDoseMessagingService : FirebaseMessagingService() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID_AVAILABILITY = "availability"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data

        val title = data["title"] ?: ""
        val body = data["body"] ?: ""
        val department = data["department"] ?: ""
        val centerId = data["center"] ?: ""

        sendNotification(this, title, body, department, centerId)
    }

    private fun sendNotification(context: Context, title: String, body: String, department: String, centerId: String) {
        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID_AVAILABILITY, "Disponibilités",
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = "Nouvelle disponibilité dans vos centre suivis avec notification";
            notificationManager.createNotificationChannel(notificationChannel);
        }

        val intent = Intent(this, BookmarkActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(BookmarkActivity.EXTRA_DEPARTMENT, department)
                .putExtra(BookmarkActivity.EXTRA_CENTER_ID, centerId)

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_AVAILABILITY)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_vmd_logo_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)

        val notificationId = "$department\\_$centerId".hashCode() // use unique id to replace notification if already exists

        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}