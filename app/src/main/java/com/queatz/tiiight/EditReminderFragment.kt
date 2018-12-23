package com.queatz.tiiight

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.queatz.tiiight.managers.DataManager
import com.queatz.tiiight.models.ReminderModel
import kotlinx.android.synthetic.main.activity_edit_reminder.*

class EditReminderFragment : Fragment() {

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
    }

    private fun setReminderId(id: Long) {
        val reminder = app.on(DataManager::class).box(ReminderModel::class).get(id)
        reminder?.let {
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
}
