package com.queatz.tiiight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.queatz.tiiight.managers.AlarmManager
import com.queatz.tiiight.managers.DataManager
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.models.ReminderModel_
import com.queatz.tiiight.views.app
import io.objectbox.android.AndroidScheduler


class DeviceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            app.on(DataManager::class).box(ReminderModel::class).query()
                .notEqual(ReminderModel_.text, "")
                .equal(ReminderModel_.done, false)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer {
                    it.forEach { app.on(AlarmManager::class).schedule(it) }
                }
        }
    }
}