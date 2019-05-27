package com.queatz.tiiight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.queatz.tiiight.App.Companion.app
import com.queatz.tiiight.managers.DataManager
import com.queatz.tiiight.managers.NotificationManager
import com.queatz.tiiight.models.ReminderModel

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            app<DataManager>().box(ReminderModel::class).get(intent.data!!.schemeSpecificPart!!.toLong())?.let {
                app<NotificationManager>().notify(it)
            }
        }
    }
}