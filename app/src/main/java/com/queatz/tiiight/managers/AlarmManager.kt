package com.queatz.tiiight.managers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.queatz.on.On
import com.queatz.on.OnLifecycle
import com.queatz.tiiight.AlarmReceiver
import com.queatz.tiiight.models.ReminderModel
import java.util.*


class AlarmManager constructor(private val on: On) : OnLifecycle {

    private lateinit var alarm: AlarmManager

    override fun on() {
        alarm = on<AppManager>().app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    fun schedule(reminder: ReminderModel) {
        cancel(reminder)

        if (reminder.date.after(Date())) {
            alarm.setExact(
                AlarmManager.RTC_WAKEUP,
                reminder.date.time,
                reminder.toPendingIntent(on<AppManager>().app)
            )
        }
    }

    fun cancel(reminder: ReminderModel) {
        alarm.cancel(reminder.toPendingIntent(on<AppManager>().app))
    }
}

fun ReminderModel.toPendingIntent(context: Context): PendingIntent {
    val alarmIntent = Intent(context, AlarmReceiver::class.java)
    alarmIntent.action = Intent.ACTION_VIEW
    alarmIntent.data = Uri.fromParts("tiiight", objectBoxId.toString(), null)
    return PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
}