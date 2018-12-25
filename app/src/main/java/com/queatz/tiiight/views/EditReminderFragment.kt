package com.queatz.tiiight.views

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.queatz.tiiight.R
import com.queatz.tiiight.managers.AlarmManager
import com.queatz.tiiight.managers.DataManager
import com.queatz.tiiight.managers.SettingsManager
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.on
import com.queatz.tiiight.showKeyboard
import com.queatz.tiiight.visible
import kotlinx.android.synthetic.main.activity_edit_reminder.*
import java.text.SimpleDateFormat
import java.util.*


class EditReminderFragment : Fragment(), ShareableFragment {

    private val dateFormat = SimpleDateFormat("EE, MMM dd, yyyy h:mma", Locale.US)

    companion object {
        private const val REMINDER_ID = "reminder"
        private const val QUICK_EDIT = "quickEdit"

        fun create(id: Long, quickEdit: Boolean): EditReminderFragment {
            val fragment = EditReminderFragment()
            fragment.arguments = Bundle()
            fragment.arguments?.putLong(REMINDER_ID, id)
            fragment.arguments?.putBoolean(QUICK_EDIT, quickEdit)
            return fragment
        }
    }

    private var isQuickEdit: Boolean = false
    private var reminder: ReminderModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(com.queatz.tiiight.R.layout.activity_edit_reminder, container, false)!!

    override fun onResume() {
        super.onResume()

        if (!isQuickEdit) {
            reminderText.requestFocus()
            reminderText.showKeyboard(true)
        }
    }

    override fun onPause() {
        super.onPause()
        reminderText.showKeyboard(false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.getLong(REMINDER_ID, -1).takeIf { it != -1L }?.let { setReminderId(it) }
        isQuickEdit = arguments?.getBoolean(QUICK_EDIT, false) ?: false

        reminderText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.post { activity?.onBackPressed() }
                true
            } else false
        }

        setTimeButton.setOnClickListener {
            reminderText.showKeyboard(false)

            if (calendarViewLayout.visible || timeView.visible) {
                calendarViewLayout.visibility = View.GONE
                timeView.visibility = View.GONE
            } else {
                calendarViewLayout.visibility = View.VISIBLE
                timeView.visibility = View.GONE
            }
        }

        reminderTimeShortcuts.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun onShare() {
        reminder?.apply {
            val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text)
            startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.share_to)))
        }
    }

    private fun setReminderId(id: Long) {
        reminder = app.on(DataManager::class).box(ReminderModel::class).get(id)
        reminder?.let {

            val cal = Calendar.getInstance()
            cal.time = it.date
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            calendarView.date = cal.time.time

            timeView.currentHour = cal.get(Calendar.HOUR_OF_DAY)
            timeView.currentMinute = cal.get(Calendar.MINUTE)

            calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                it.date = cal.time
                it.done = false
                app.on(DataManager::class).box(ReminderModel::class).put(it)
                app.on(AlarmManager::class).schedule(it)

                calendarViewLayout.visibility = View.GONE
                timeView.visibility = View.VISIBLE
                showTime(it.date)

                if (calendarViewLayout.visible || timeView.visible) {
                    saveLastDate(it.date)
                }
            }

            timeView.setOnTimeChangedListener { _, hourOfDay, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)

                it.date = cal.time
                it.done = false
                app.on(DataManager::class).box(ReminderModel::class).put(it)
                app.on(AlarmManager::class).schedule(it)
                showTime(it.date)

                if (calendarViewLayout.visible || timeView.visible) {
                    saveLastDate(it.date)
                }
            }

            showTime(it.date)

            reminderText.setText(it.text)
            reminderText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    it.text = s.toString()
                    app.on(DataManager::class).box(ReminderModel::class).put(it)
                }
            })

            val adapter = ReminderTimeShortcutAdapter { date ->
                it.date = date
                it.done = false
                app.on(DataManager::class).box(ReminderModel::class).put(it)
                app.on(AlarmManager::class).schedule(it)
                showTime(it.date)
                view?.post { activity?.onBackPressed() }
            }

            reminderTimeShortcuts.adapter = adapter

            val items = mutableListOf<ReminderTimeShortcutItem>()

            items.add(
                ReminderTimeShortcutItem(
                    com.queatz.tiiight.R.drawable.ic_schedule_black_24dp,
                    getString(com.queatz.tiiight.R.string.reminder_time_in_one_hour),
                    Calendar.getInstance().apply {
                        add(Calendar.HOUR, 1)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                )
            )

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
                                com.queatz.tiiight.R.drawable.ic_brightness_3_black_24dp,
                                getString(com.queatz.tiiight.R.string.reminder_time_tonight),
                                it
                            )
                        )
                    }
                }
            } else {
                items.add(
                    ReminderTimeShortcutItem(
                        com.queatz.tiiight.R.drawable.ic_wb_sunny_black_24dp,
                        getString(com.queatz.tiiight.R.string.reminder_time_in_the_morning),
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
                    com.queatz.tiiight.R.drawable.ic_arrow_forward_black_24dp,
                    getString(com.queatz.tiiight.R.string.reminder_time_tomorrow),
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
                    com.queatz.tiiight.R.drawable.ic_fast_forward_black_24dp,
                    getString(com.queatz.tiiight.R.string.reminder_time_in_2_days),
                    Calendar.getInstance().apply {
                        add(Calendar.DATE, 2)
                        set(Calendar.HOUR_OF_DAY, 5)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                )
            )
            items.add(
                ReminderTimeShortcutItem(
                    com.queatz.tiiight.R.drawable.ic_weekend_black_24dp,
                    getString(com.queatz.tiiight.R.string.reminder_time_next_weekend),
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
                    com.queatz.tiiight.R.drawable.ic_looks_black_24dp,
                    getString(com.queatz.tiiight.R.string.reminder_time_next_monday),
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

            app.on(SettingsManager::class).settings.lastDate.let {
                if (Date().before(it)) {
                    items.add(
                        ReminderTimeShortcutItem(
                            com.queatz.tiiight.R.drawable.ic_replay_black_24dp,
                            getString(com.queatz.tiiight.R.string.reminder_time_last),
                            it
                        )
                    )
                }
            }

            items.sortBy { it.date }

            var i = 1
            while (i < items.size) {
                if (items[i - 1].date == items[i].date) {
                    items.removeAt(i)
                } else {
                    i++
                }
            }

            adapter.items = items
        }
    }

    private fun saveLastDate(date: Date) {
        app.on(SettingsManager::class).settings.apply {
            lastDate = date
            app.on(SettingsManager::class).settings = this
        }
    }

    private fun showTime(date: Date) {
        reminderDate.text = dateFormat.format(date)
    }
}
