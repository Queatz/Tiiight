package com.queatz.tiiight.managers

import com.queatz.on.On
import com.queatz.tiiight.App
import com.queatz.tiiight.R
import com.queatz.tiiight.views.ReminderTimeShortcutItem
import java.text.SimpleDateFormat
import java.util.*

class SnoozeManager constructor(private val on: On) {

    val dateFormat = SimpleDateFormat("EE, MMM dd, h:mma", Locale.US)

    fun getSnoozeItems(includeInAnHour: Boolean, includeLastOption: Boolean, deduplicate: Boolean = true): MutableList<ReminderTimeShortcutItem> {
        val items = mutableListOf<ReminderTimeShortcutItem>()

        val getString = { stringRes: Int -> on<ContextManager>().context.getString(stringRes) }

        if (includeInAnHour) {
            items.add(
                ReminderTimeShortcutItem(
                    R.drawable.ic_schedule_black_24dp,
                    getString(R.string.reminder_time_in_one_hour),
                    Calendar.getInstance().apply {
                        add(Calendar.HOUR, 1)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                )
            )
        }

        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 5) {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 19)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time.let {
                if (Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }.time.before(it)) {
                    items.add(
                        ReminderTimeShortcutItem(
                            R.drawable.ic_brightness_3_black_24dp,
                            getString(R.string.reminder_time_tonight),
                            it
                        )
                    )
                } else {
                    items.add(
                        ReminderTimeShortcutItem(
                            R.drawable.ic_wb_sunny_black_24dp,
                            getString(R.string.reminder_time_in_the_morning),
                            Calendar.getInstance().apply {
                                add(Calendar.DATE, 1)
                                set(Calendar.HOUR_OF_DAY, 5)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.time
                        )
                    )
                }
            }
        } else {
            items.add(
                ReminderTimeShortcutItem(
                    R.drawable.ic_wb_sunny_black_24dp,
                    getString(R.string.reminder_time_in_the_morning),
                    Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 5)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                )
            )
        }

        items.add(
            ReminderTimeShortcutItem(
                R.drawable.ic_arrow_forward_black_24dp,
                getString(R.string.reminder_time_tomorrow),
                Calendar.getInstance().apply {
                    add(Calendar.DATE, 1)
                    set(Calendar.HOUR_OF_DAY, 5)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            )
        )
        items.add(
            ReminderTimeShortcutItem(
                R.drawable.ic_brightness_medium_black_24dp,
                getString(R.string.reminder_time_tomorrow_night),
                Calendar.getInstance().apply {
                    add(Calendar.DATE, 1)
                    set(Calendar.HOUR_OF_DAY, 19)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            )
        )
        items.add(
            ReminderTimeShortcutItem(
                R.drawable.ic_weekend_black_24dp,
                getString(R.string.reminder_time_next_weekend),
                Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, 7)
                    set(Calendar.HOUR_OF_DAY, 5)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)

                    if (Date().after(time)) {
                        add(Calendar.WEEK_OF_YEAR, 1)
                    }
                }.time
            )
        )
        items.add(
            ReminderTimeShortcutItem(
                R.drawable.ic_looks_black_24dp,
                getString(R.string.reminder_time_next_monday),
                Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, 2)
                    set(Calendar.HOUR_OF_DAY, 5)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)

                    if (Date().after(time)) {
                        add(Calendar.WEEK_OF_YEAR, 1)
                    }
                }.time
            )
        )
        items.add(
            ReminderTimeShortcutItem(
                R.drawable.ic_fast_forward_black_24dp,
                getString(R.string.reminder_time_in_2_days),
                Calendar.getInstance().apply {
                    add(Calendar.DATE, 2)
                    set(Calendar.HOUR_OF_DAY, 5)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            )
        )

        if (includeLastOption) {
            App.app<SettingsManager>().settings.lastDate.let {
                if (Date().before(it)) {
                    items.add(
                        ReminderTimeShortcutItem(
                            R.drawable.ic_replay_black_24dp,
                            getString(R.string.reminder_time_last),
                            it
                        )
                    )
                }
            }
        }

        items.sortBy { it.date }

        var i = 1

        if (deduplicate) while (i < items.size) {
            if (items[i - 1].date == items[i].date) {
                items.removeAt(i)
            } else {
                i++
            }
        }

        return items
    }
}