package com.queatz.tiiight.managers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.queatz.tiiight.PoolMember
import com.queatz.tiiight.R
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.views.MainActivity

class NotificationManager : PoolMember() {

    companion object {
        private const val NOTIFICATION_ID = 0
        private const val REQUEST_CODE_NOTIFICATION = 101
    }

    fun notify(reminder: ReminderModel) {
        val intent = Intent(on(AppManager::class).app, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            on(AppManager::class).app,
            REQUEST_CODE_NOTIFICATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                on(AppManager::class).app.getString(R.string.tiiight_notifications),
                on(AppManager::class).app.getString(R.string.tiiight_notifications),
                NotificationManager.IMPORTANCE_HIGH
            )
            (on(AppManager::class).app.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(on(AppManager::class).app, on(AppManager::class).app.getString(R.string.tiiight_notifications))
            .setSmallIcon(R.drawable.ic_check_circle_white_24dp)
            .setContentTitle(reminder.text)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setDefaults(Notification.DEFAULT_ALL)
            .setColor(on(AppManager::class).app.resources.getColor(R.color.colorPrimary))
            .build()

        NotificationManagerCompat.from(on(AppManager::class).app).notify("tiiight:" + reminder.objectBoxId.toString(), NOTIFICATION_ID, notification)
    }
}