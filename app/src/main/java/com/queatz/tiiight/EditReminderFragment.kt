package com.queatz.tiiight

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.queatz.tiiight.managers.DataManager
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
    }

    private fun setReminderId(id: Long) {
        val reminder = app.on(DataManager::class).box(ReminderModel::class).get(id)
        reminder?.let {

            val cal = Calendar.getInstance()
            cal.time = reminder.date

            calendarView.date = reminder.date.time

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
                showTime(reminder.date)
            }

            timeView.setOnTimeChangedListener { _, hourOfDay, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)

                it.date = cal.time
                app.on(DataManager::class).box(ReminderModel::class).put(it)
                showTime(reminder.date)
            }

            showTime(reminder.date)

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
        }
    }

    private fun showTime(date: Date) {
        reminderDate.text = dateFormat.format(date)
    }
}
