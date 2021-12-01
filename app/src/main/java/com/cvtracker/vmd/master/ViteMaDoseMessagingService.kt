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
        const val NOTIFICATION_CHANNEL_ID_CHRONODOSE = "chronodose"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data

        val title = data["title"] ?: ""
        val body = data["body"] ?: ""
        val department = data["department"] ?: ""
        val centerId = data["center"] ?: ""
        val topic = data["topic"] ?: ""
        val type = data["type"] ?: ""

        sendNotification(this, title, body, department, centerId, topic, type)

        AnalyticsHelper.logEventNotificationReceive(department, centerId, topic, type)
    }

    private fun sendNotification(
        context: Context,
        title: String,
        body: String,
        department: String,
        centerId: String,
        topic: String,
        type: String
    ) {
        val notificationManager = NotificationManagerCompat.from(context)

        val notificationId = topic.hashCode() // use unique id to replace notification if already exists

        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID_AVAILABILITY, getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = getString(R.string.notification_channel_description)
            notificationManager.createNotificationChannel(notificationChannel)

            val chronodoseChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID_CHRONODOSE, getString(R.string.notification_chronodose_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            chronodoseChannel.description = getString(R.string.notification_chronodose_channel_description)
            notificationManager.createNotificationChannel(chronodoseChannel);
        }

        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .setData(Uri.parse("${MainPresenter.BASE_URL}/bookmark/$department/$centerId/$topic/$type"))

        val notificationFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, notificationId, intent, notificationFlags)

        val intentAction = SilentRedirectReceiver.buildIntent(this, department, centerId, topic, type, notificationId)
        val actionPendingIntent = PendingIntent.getBroadcast(this, notificationId, intentAction, notificationFlags)

        val (channelId: String, iconResId: Int) = if(FcmHelper.isTopicChronodose(topic)){
            NOTIFICATION_CHANNEL_ID_CHRONODOSE to R.drawable.ic_lightning_charge_fill_24dp
        }else{
            NOTIFICATION_CHANNEL_ID_AVAILABILITY to R.drawable.ic_vmd_logo_notification
        }

        val notificationBuilder =
            NotificationCompat.Builder(context, channelId)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(iconResId)
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