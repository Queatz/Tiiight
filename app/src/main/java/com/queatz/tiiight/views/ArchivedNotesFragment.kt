package com.queatz.tiiight.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.queatz.tiiight.App.Companion.app
import com.queatz.tiiight.R
import com.queatz.tiiight.managers.AlarmManager
import com.queatz.tiiight.managers.DataManager
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.models.ReminderModel_
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class ArchivedNotesFragment : Fragment() {
    companion object {
        fun create() = ArchivedNotesFragment()
    }

    private var remindersSubscription: DataSubscription? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.activity_archived_notes, container, false)!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = ReminderAdapter({ edit(it) }, resources)
        adapter.mainActionIconResId = R.drawable.ic_unarchive_white_24dp
        reminders.adapter = adapter
        reminders.layoutManager = LinearLayoutManager(context)

        ItemTouchHelper(SwipeOptions(adapter, resources, { reminder ->
            reminder.done = false
            app<DataManager>().box(ReminderModel::class).put(reminder)
            app<AlarmManager>().schedule(reminder)

            Snackbar.make(activity?.findViewById(R.id.coordinator)!!, getString(R.string.unarchived), Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo) {
                    reminder.done = true
                    reminder.doneDate = Date()
                    app<DataManager>().box(ReminderModel::class).put(reminder)
                    app<AlarmManager>().cancel(reminder)
                }
                .show()
        }, {
            edit(it, true)
        })).attachToRecyclerView(reminders)

        remindersSubscription = app<DataManager>().box(ReminderModel::class).query()
            .notEqual(ReminderModel_.text, "")
            .equal(ReminderModel_.done, true)
            .sort { o1, o2 -> (o2.doneDate ?: Date(0)).compareTo(o1.doneDate ?: Date(0)) }
            .build()
            .subscribe()
            .on(AndroidScheduler.mainThread())
            .observer { adapter.items = it }
    }

    override fun onDestroy() {
        remindersSubscription?.cancel()
        super.onDestroy()
    }

    private fun edit(reminder: ReminderModel, quickEdit: Boolean = false) {
        (activity as MainActivity).showFragment(EditReminderFragment.create(reminder.objectBoxId, quickEdit), getString(R.string.edit_reminder))
    }
}
