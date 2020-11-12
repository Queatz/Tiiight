package com.queatz.tiiight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.queatz.tiiight.App.Companion.app
import com.queatz.tiiight.managers.*
import com.queatz.tiiight.models.ReminderModel
import java.util.*


class DoneReceiver : BroadcastReceiver() {

    companion object {
        const val REMINDER_ID = "reminderId"
        const val SNOOZE_TO_TEXT = "snoozeToText"
        const val MARK_DONE = "markDone"
    }

    private val on = app

    override fun onReceive(context: Context, intent: Intent?) {
        on<ContextManager>().context = context

        intent?.getLongExtra(REMINDER_ID, -1)?.let { reminderId ->
            if (reminderId == -1L) {
                return
            }

            if (intent.hasExtra(MARK_DONE)) {
                on<DataManager>().box(ReminderModel::class).get(reminderId)?.let {
                    it.done = true
                    it.doneDate = Date()
                    on<DataManager>().box(ReminderModel::class).put(it)
                    on<ToastManager>().show(R.string.marked_done)

                    on<NotificationManager>().dismiss(it)
                }
            } else if (intent.hasExtra(SNOOZE_TO_TEXT)) {
                val text = intent.getStringExtra(SNOOZE_TO_TEXT)

                val date = on<SnoozeManager>().getSnoozeItems(false, false, false).firstOrNull {
                    it.text == text
                }?.date

                if (date == null) {
                    on<ToastManager>().show(on<ContextManager>().context.getString(R.string.snooze_denied))
                    return@let
                }

                on<DataManager>().box(ReminderModel::class).get(reminderId)?.let {
                    it.done = false
                    it.doneDate = null
                    it.date = date

                    on<DataManager>().box(ReminderModel::class).put(it)
                    on<AlarmManager>().schedule(it)

                    on<ToastManager>().show(on<ContextManager>().context.getString(R.string.snoozed_until, on<SnoozeManager>().dateFormat.format(date)))

                    on<NotificationManager>().dismiss(it)
                }
            }

            return
        }
    }
}