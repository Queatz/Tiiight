package com.queatz.tiiight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.queatz.tiiight.App.Companion.app
import com.queatz.tiiight.managers.AlarmManager
import com.queatz.tiiight.managers.DataManager
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.models.ReminderModel_
import io.objectbox.android.AndroidScheduler
import java.util.*


class DeviceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            val oneWeekAhead = Calendar.getInstance()
            oneWeekAhead.add(Calendar.WEEK_OF_MONTH, 1)

            app<DataManager>().box(ReminderModel::class).query()
                .notEqual(ReminderModel_.text, "")
                .equal(ReminderModel_.done, false)
                .less(ReminderModel_.date, oneWeekAhead.time)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer {
                    it.forEach { app<AlarmManager>().schedule(it) }
                }
        }
    }
}