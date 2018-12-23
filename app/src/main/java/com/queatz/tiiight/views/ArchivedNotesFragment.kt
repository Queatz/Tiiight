package com.queatz.tiiight.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.queatz.tiiight.R
import com.queatz.tiiight.managers.DataManager
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.models.ReminderModel_
import com.queatz.tiiight.on
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import kotlinx.android.synthetic.main.content_main.*

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
        adapter.mainActionIconResId = R.drawable.ic_unarchive_black_24dp
        adapter.pastRemindersSectionHeaderName = R.string.archive
        reminders.adapter = adapter
        reminders.layoutManager = LinearLayoutManager(context)

        ItemTouchHelper(SwipeOptions(adapter, resources, { reminder ->
            reminder.done = false
            app.on(DataManager::class).box(ReminderModel::class).put(reminder)

            Snackbar.make(activity?.findViewById(R.id.coordinator)!!, "Unarchived", Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo) {
                    reminder.done = true
                    app.on(DataManager::class).box(ReminderModel::class).put(reminder)
                }
                .show()
        }, {
            edit(it, true)
        }, { reminder, other ->
            reminder.date = other.date
            app.on(DataManager::class).box(ReminderModel::class).put(reminder)
        })).attachToRecyclerView(reminders)

        remindersSubscription = app.on(DataManager::class).box(ReminderModel::class).query()
            .notEqual(ReminderModel_.text, "")
            .equal(ReminderModel_.done, true)
            .sort { o1, o2 -> o2.date.compareTo(o1.date) }
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
