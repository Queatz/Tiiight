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
import com.queatz.tiiight.DoneReceiver.Companion.MARK_DONE
import com.queatz.tiiight.DoneReceiver.Companion.REMINDER_ID
import com.queatz.tiiight.DoneReceiver.Companion.SNOOZE_TO_TEXT
import com.queatz.tiiight.R
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.views.MainActivity
import kotlin.random.Random


class NotificationManager constructor(private val on: On) {

    companion object {
        private const val NOTIFICATION_ID = 0
    }

    fun notify(reminder: ReminderModel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                on<AppManager>().app.getString(R.string.tiiight_notifications),
                on<AppManager>().app.getString(R.string.tiiight_notifications),
                NotificationManager.IMPORTANCE_HIGH
            )
            (on<AppManager>().app.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        val doneIndent = PendingIntent.getBroadcast(on<AppManager>().app, Random.nextInt(),
            Intent(on<AppManager>().app, DoneReceiver::class.java)
                .putExtra(REMINDER_ID, reminder.objectBoxId)
                .putExtra(MARK_DONE, true), PendingIntent.FLAG_CANCEL_CURRENT)

        val snoozie = on<SnoozeManager>().getSnoozeItems(false, false).first()

        val snoozeIndent = PendingIntent.getBroadcast(on<AppManager>().app, Random.nextInt(),
            Intent(on<AppManager>().app, DoneReceiver::class.java)
                .putExtra(REMINDER_ID, reminder.objectBoxId)
                .putExtra(SNOOZE_TO_TEXT, snoozie.text), PendingIntent.FLAG_CANCEL_CURRENT)

        val contentIndent = PendingIntent.getActivity(on<AppManager>().app, Random.nextInt(),
            Intent(on<AppManager>().app, MainActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .putExtra(REMINDER_ID, reminder.objectBoxId), PendingIntent.FLAG_CANCEL_CURRENT)

        val notification = NotificationCompat.Builder(on<AppManager>().app, on<AppManager>().app.getString(R.string.tiiight_notifications))
            .setSmallIcon(R.drawable.ic_check_circle_white_24dp)
            .setContentTitle(reminder.text)
            .setAutoCancel(true)
            .addAction(0, on<AppManager>().app.getString(R.string.done), doneIndent)
            .addAction(0, snoozie.text, snoozeIndent)
            .setContentIntent(contentIndent)
            .setDefaults(Notification.DEFAULT_ALL)
            .setColor(on<AppManager>().app.resources.getColor(R.color.colorPrimary))
            .build()

        NotificationManagerCompat.from(on<AppManager>().app).notify("tiiight:" + reminder.objectBoxId.toString(), NOTIFICATION_ID, notification)
    }

    fun dismiss(reminder: ReminderModel) {
        NotificationManagerCompat.from(on<AppManager>().app).cancel("tiiight:" + reminder.objectBoxId.toString(), NOTIFICATION_ID)
    }
}