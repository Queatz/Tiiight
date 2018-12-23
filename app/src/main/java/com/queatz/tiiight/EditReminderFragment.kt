package com.queatz.tiiight

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.queatz.tiiight.managers.DataManager
import com.queatz.tiiight.managers.SettingsManager
import com.queatz.tiiight.models.ReminderModel
import kotlinx.android.synthetic.main.activity_edit_reminder.*
import java.text.SimpleDateFormat
import java.util.*


class EditReminderFragment : Fragment() {

    private val dateFormat = SimpleDateFormat("EE, MMM dd, yyyy h:mma", Locale.US)

    companion object {
        private const val REMINDER_ID = "reminder"

        fun create(id: Long): EditReminderFragment {
            val fragment = EditReminderFragment()
            fragment.arguments = Bundle()
            fragment.arguments?.putLong(REMINDER_ID, id)
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.activity_edit_reminder, container, false)

    override fun onResume() {
        super.onResume()
        reminderText.requestFocus()
        reminderText.showKeyboard(true)
    }

    override fun onPause() {
        super.onPause()
        reminderText.showKeyboard(false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.getLong(REMINDER_ID, -1).takeIf { it != -1L }?.let { setReminderId(it) }

        reminderText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.post { activity?.onBackPressed() }
                true
            } else false
        }

        setTimeButton.setOnClickListener {
            reminderText.showKeyboard(false)

            if (calendarViewLayout.visibility == View.VISIBLE || timeView.visibility == View.VISIBLE) {
                calendarViewLayout.visibility = View.GONE
                timeView.visibility = View.GONE
            } else {
                calendarViewLayout.visibility = View.VISIBLE
                timeView.visibility = View.GONE
            }
        }

        reminderTimeShortcuts.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setReminderId(id: Long) {
        val reminder = app.on(DataManager::class).box(ReminderModel::class).get(id)
        reminder?.let {

            val cal = Calendar.getInstance()
            cal.time = it.date

            calendarView.date = it.date.time

            timeView.currentHour = cal.get(Calendar.HOUR_OF_DAY)
            timeView.currentMinute = cal.get(Calendar.MINUTE)

            calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                it.date = cal.time
                app.on(DataManager::class).box(ReminderModel::class).put(it)

                calendarViewLayout.visibility = View.GONE
                timeView.visibility = View.VISIBLE
                showTime(it.date)

                if (calendarViewLayout.visibility == View.VISIBLE || timeView.visibility == View.VISIBLE) {
                    saveLastDate(it.date)
                }
            }

            timeView.setOnTimeChangedListener { _, hourOfDay, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)

                it.date = cal.time
                app.on(DataManager::class).box(ReminderModel::class).put(it)
                showTime(it.date)

                if (calendarViewLayout.visibility == View.VISIBLE || timeView.visibility == View.VISIBLE) {
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
                app.on(DataManager::class).box(ReminderModel::class).put(it)
                showTime(it.date)
            }

            reminderTimeShortcuts.adapter = adapter

            val items = mutableListOf<ReminderTimeShortcutItem>()

            items.add(ReminderTimeShortcutItem(R.drawable.ic_schedule_black_24dp, "In 1 hour", Calendar.getInstance().apply {
                add(Calendar.HOUR, 1)
            }.time))

            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 19)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time.let {
                if (Date().before(it)) {
                    items.add(ReminderTimeShortcutItem(R.drawable.ic_brightness_3_black_24dp, "Tonight", it))
                }
            }

            items.add(ReminderTimeShortcutItem(R.drawable.ic_arrow_forward_black_24dp, "Tomorrow", Calendar.getInstance().apply {
                add(Calendar.DATE, 1)
                set(Calendar.HOUR_OF_DAY, 5)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time))
            items.add(ReminderTimeShortcutItem(R.drawable.ic_fast_forward_black_24dp, "In 2 days", Calendar.getInstance().apply {
                add(Calendar.DATE, 2)
                set(Calendar.HOUR_OF_DAY, 5)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time))
            items.add(ReminderTimeShortcutItem(R.drawable.ic_weekend_black_24dp, "Next weekend", Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, 7)
                set(Calendar.HOUR_OF_DAY, 5)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (Date().after(time)) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }.time))
            items.add(ReminderTimeShortcutItem(R.drawable.ic_wb_sunny_black_24dp, "Next Monday", Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, 2)
                set(Calendar.HOUR_OF_DAY, 5)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (Date().after(time)) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }.time))

            app.on(SettingsManager::class).settings.lastDate.let {
                if (Date().before(it)) {
                    items.add(ReminderTimeShortcutItem(R.drawable.ic_replay_black_24dp, "Last", it))
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
