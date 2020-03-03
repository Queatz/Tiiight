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
import com.queatz.on.On
import com.queatz.tiiight.DoneReceiver
import com.queatz.tiiight.DoneReceiver.Companion.REMINDER_ID
import com.queatz.tiiight.R
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.views.MainActivity


class NotificationManager constructor(private val on: On) {

    companion object {
        private const val NOTIFICATION_ID = 0
        private const val REQUEST_CODE_NOTIFICATION = 101
    }

    fun notify(reminder: ReminderModel) {
        val intent = Intent(on<AppManager>().app, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            on<AppManager>().app,
            REQUEST_CODE_NOTIFICATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                on<AppManager>().app.getString(R.string.tiiight_notifications),
                on<AppManager>().app.getString(R.string.tiiight_notifications),
                NotificationManager.IMPORTANCE_HIGH
            )
            (on<AppManager>().app.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        val doneIndent = PendingIntent.getBroadcast(on<AppManager>().app, 0,
            Intent(on<AppManager>().app, DoneReceiver::class.java).putExtra(REMINDER_ID, reminder.objectBoxId), 0)
        val snoozeIndent = PendingIntent.getActivity(on<AppManager>().app, 0,
            Intent(on<AppManager>().app, MainActivity::class.java).setAction(Intent.ACTION_VIEW).putExtra(REMINDER_ID, reminder.objectBoxId), 0)

        val notification = NotificationCompat.Builder(on<AppManager>().app, on<AppManager>().app.getString(R.string.tiiight_notifications))
            .setSmallIcon(R.drawable.ic_check_circle_white_24dp)
            .setContentTitle(reminder.text)
            .setAutoCancel(true)
            .addAction(0, on<AppManager>().app.getString(R.string.done), doneIndent)
            .addAction(0, on<AppManager>().app.getString(R.string.snooze), snoozeIndent)
            .setContentIntent(contentIntent)
            .setDefaults(Notification.DEFAULT_ALL)
            .setColor(on<AppManager>().app.resources.getColor(R.color.colorPrimary))
            .build()

        NotificationManagerCompat.from(on<AppManager>().app).notify("tiiight:" + reminder.objectBoxId.toString(), NOTIFICATION_ID, notification)
    }

    fun dismiss(reminder: ReminderModel) {
        NotificationManagerCompat.from(on<AppManager>().app).cancel("tiiight:" + reminder.objectBoxId.toString(), NOTIFICATION_ID)
    }
}