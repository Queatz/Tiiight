package com.queatz.tiiight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.queatz.tiiight.App.Companion.app
import com.queatz.tiiight.managers.ContextManager
import com.queatz.tiiight.managers.DataManager
import com.queatz.tiiight.managers.NotificationManager
import com.queatz.tiiight.managers.ToastManager
import com.queatz.tiiight.models.ReminderModel
import java.util.*


class DoneReceiver : BroadcastReceiver() {

    companion object {
        const val REMINDER_ID = "reminderId"
    }

    private val on = app

    override fun onReceive(context: Context, intent: Intent?) {
        on<ContextManager>().context = context

        intent?.getLongExtra(REMINDER_ID, -1)!!.let { reminderId ->
            if (reminderId == -1L) {
                return
            }

            on<DataManager>().box(ReminderModel::class).get(reminderId)?.let {
                it.done = true
                it.doneDate = Date()
                on<DataManager>().box(ReminderModel::class).put(it)
                on<ToastManager>().show(R.string.marked_done)

                on<NotificationManager>().dismiss(it)
            }
        }
    }
}