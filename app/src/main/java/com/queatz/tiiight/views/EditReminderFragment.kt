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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.queatz.tiiight.App.Companion.app
import com.queatz.tiiight.R
import com.queatz.tiiight.managers.*
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.showKeyboard
import com.queatz.tiiight.visible
import kotlinx.android.synthetic.main.activity_edit_reminder.*
import java.text.SimpleDateFormat
import java.util.*


class EditReminderFragment : Fragment(), ShareableFragment {

    private val dateFormat = SimpleDateFormat("EE, MMM dd, yyyy h:mma", Locale.US)
    private lateinit var filterAdapter: FilterAdapter

    companion object {
        private const val REMINDER_ID = "reminder"
        private const val QUICK_EDIT = "quickEdit"
        private const val IS_CREATE = "isCreate"

        fun create(id: Long, quickEdit: Boolean, isCreate: Boolean = false): EditReminderFragment {
            val fragment = EditReminderFragment()
            fragment.arguments = Bundle()
            fragment.arguments?.putLong(REMINDER_ID, id)
            fragment.arguments?.putBoolean(QUICK_EDIT, quickEdit)
            fragment.arguments?.putBoolean(IS_CREATE, isCreate)
            return fragment
        }
    }

    private var isQuickEdit: Boolean = false
    private var isCreate: Boolean = false
    private var reminder: ReminderModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.activity_edit_reminder, container, false)!!

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
        isCreate = arguments?.getBoolean(IS_CREATE, false) ?: false

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

        reminderTimeShortcuts.layoutManager = GridLayoutManager(context, 3)


        filterAdapter = FilterAdapter(false) {
            reminderText.setText(it + " " + reminderText.text)
            reminderText.setSelection(it.length + 1)
        }

        filters.adapter = filterAdapter
        filters.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        filterAdapter.items = app<FilterManager>().getTopFilters().toMutableList()
    }

    override fun onShare() {
        reminder?.apply {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.share_to)))
        }
    }

    override fun onArchive() {
        reminder?.let {
            it.done = true
            it.doneDate = Date()
            app<DataManager>().box(ReminderModel::class).put(it)
            app<AlarmManager>().cancel(it)
        }

        view?.post { activity?.onBackPressed() }
    }

    override fun onUnarchive() {
        reminder?.let {
            it.done = false
            app<DataManager>().box(ReminderModel::class).put(it)
            app<AlarmManager>().schedule(it)
        }

        view?.post { activity?.onBackPressed() }
    }

    override fun showShare() = !isCreate
    override fun showUnarchive() = !isCreate && reminder?.done ?: false
    override fun showArchive() = !isCreate && reminder?.done?.not() ?: false

    private fun setReminderId(id: Long) {
        reminder = app<DataManager>().box(ReminderModel::class).get(id)
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
                app<DataManager>().box(ReminderModel::class).put(it)
                app<AlarmManager>().schedule(it)

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
                app<DataManager>().box(ReminderModel::class).put(it)
                app<AlarmManager>().schedule(it)
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
                    app<DataManager>().box(ReminderModel::class).put(it)
                }
            })

            val adapter = ReminderTimeShortcutAdapter { date ->
                it.date = date
                it.done = false
                app<DataManager>().box(ReminderModel::class).put(it)
                app<AlarmManager>().schedule(it)
                showTime(it.date)

                if (it.text.isNotBlank()) {
                    view?.post { activity?.onBackPressed() }
                }
            }

            reminderTimeShortcuts.adapter = adapter

            adapter.items = app<SnoozeManager>().getSnoozeItems(true, true)
        }
    }

    private fun saveLastDate(date: Date) {
        app<SettingsManager>().settings.apply {
            lastDate = date
            app<SettingsManager>().settings = this
        }
    }

    private fun showTime(date: Date) {
        reminderDate.text = dateFormat.format(date)
        reminderText.hint = resources.getString(R.string.edit_reminder_hint, dateFormat.format(date))
    }
}
