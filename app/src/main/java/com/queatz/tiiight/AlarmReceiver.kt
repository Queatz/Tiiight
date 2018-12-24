package com.queatz.tiiight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.queatz.tiiight.managers.DataManager
import com.queatz.tiiight.managers.NotificationManager
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.views.app

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            app.on(DataManager::class).box(ReminderModel::class).get(intent.data!!.schemeSpecificPart!!.toLong())?.let {
                app.on(NotificationManager::class).notify(it)
            }
        }
    }
}