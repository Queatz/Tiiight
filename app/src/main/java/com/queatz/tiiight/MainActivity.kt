package com.queatz.tiiight

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.queatz.tiiight.managers.ContextManager
import com.queatz.tiiight.managers.DataManager
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.models.ReminderModel_
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private var remindersSubscription: DataSubscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        app.on(ContextManager::class).context = this

        fab.setOnClickListener { view ->
            val reminder = ReminderModel("", false, Date())
            app.on(DataManager::class).box(ReminderModel::class).put(reminder)
            edit(reminder)
        }

        val adapter = ReminderAdapter({ edit(it) })
        reminders.adapter = adapter
        reminders.layoutManager = LinearLayoutManager(this)

        ItemTouchHelper(SwipeOptions(adapter, resources, { reminder ->
            reminder.done = true
            app.on(DataManager::class).box(ReminderModel::class).put(reminder)

            Snackbar.make(coordinator, "Marked done", Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo) {
                    reminder.done = false
                    app.on(DataManager::class).box(ReminderModel::class).put(reminder)
                }
                .show()
        }, {}, { reminder, other ->
            reminder.date = other.date
            app.on(DataManager::class).box(ReminderModel::class).put(reminder)
        })).attachToRecyclerView(reminders)

        remindersSubscription = app.on(DataManager::class).box(ReminderModel::class).query()
            .notEqual(ReminderModel_.text, "")
            .equal(ReminderModel_.done, false)
            .sort { o1, o2 -> o2.date.compareTo(o1.date) }
            .build()
            .subscribe()
            .on(AndroidScheduler.mainThread())
            .observer { adapter.items = it }
    }

    private fun edit(reminder: ReminderModel) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .add(R.id.content, EditReminderFragment.create(reminder.objectBoxId))
            .commit()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
            setDisplayShowHomeEnabled(supportFragmentManager.backStackEntryCount > 0)
        }
    }

    override fun onDestroy() {
        remindersSubscription?.cancel()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}

val app = 0