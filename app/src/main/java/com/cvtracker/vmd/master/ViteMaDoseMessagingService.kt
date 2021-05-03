package com.cvtracker.vmd.master

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cvtracker.vmd.R
import com.cvtracker.vmd.home.MainActivity
import com.cvtracker.vmd.home.MainPresenter
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

    private fun sendNotification(
        context: Context,
        title: String,
        body: String,
        department: String,
        centerId: String
    ) {
        val notificationManager = NotificationManagerCompat.from(context)

        val notificationId = "$department\\_$centerId".hashCode() // use unique id to replace notification if already exists

        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID_AVAILABILITY, "Disponibilités",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = getString(R.string.notification_channel_description)
            notificationManager.createNotificationChannel(notificationChannel);
        }

        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .setData(Uri.parse("${MainPresenter.BASE_URL}/bookmark/$department/$centerId"))

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        );

        val intentAction = Intent(this, SilentRedirectActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .setData(Uri.parse("${SilentRedirectActivity.DISABLE_NOTIFICATION_LINK}/$department/$centerId/$notificationId"))
        val actionPendingIntent = PendingIntent.getActivity(this, 0, intentAction, 0)

        val notificationBuilder =
            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_AVAILABILITY)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_vmd_logo_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .addAction(
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_notifications_off_black_24dp,
                        getString(R.string.notification_disable_notifications),
                        actionPendingIntent
                    ).build()
                )

        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}