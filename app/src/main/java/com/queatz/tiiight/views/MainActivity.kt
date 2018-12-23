package com.queatz.tiiight.views

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.queatz.tiiight.R
import com.queatz.tiiight.managers.ContextManager
import com.queatz.tiiight.managers.DataManager
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.models.ReminderModel_
import com.queatz.tiiight.on
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private var remindersSubscription: DataSubscription? = null
    private var settingsButton: MenuItem? = null

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

        val adapter = ReminderAdapter({ edit(it) }, resources)
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
        }, {
            edit(it, true)
        }, { reminder, other ->
            reminder.date = other.date
            app.on(DataManager::class).box(ReminderModel::class).put(reminder)
        })).attachToRecyclerView(reminders)

        remindersSubscription = app.on(DataManager::class).box(ReminderModel::class).query()
            .notEqual(ReminderModel_.text, "")
            .equal(ReminderModel_.done, false)
            .sort { o1, o2 -> o1.date.compareTo(o2.date) }
            .build()
            .subscribe()
            .on(AndroidScheduler.mainThread())
            .observer { adapter.items = it }
    }

    override fun onResume() {
        super.onResume()
        refreshUpButton()
    }

    override fun onDestroy() {
        remindersSubscription?.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        refreshUpButton()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        settingsButton = menu.findItem(R.id.action_settings)
        settingsButton?.isVisible = supportFragmentManager.backStackEntryCount <= 0

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_settings -> {
                showFragment(SettingsFragment.create(), getString(R.string.action_settings))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun edit(reminder: ReminderModel, quickEdit: Boolean = false) {
        showFragment(EditReminderFragment.create(reminder.objectBoxId, quickEdit), getString(R.string.edit_reminder))
    }

    internal fun showFragment(fragment: Fragment, name: String? = null) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(name)
            .add(R.id.content, fragment)
            .commit()
        supportActionBar?.apply {
            reminders.post { refreshUpButton() }
        }
    }

    private fun refreshUpButton() {
        val hasBackStack = supportFragmentManager.backStackEntryCount > 0

        supportActionBar?.apply {
            title = if (hasBackStack) {
                supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name ?: getString(R.string.app_name)
            } else {
                getString(R.string.app_name)
            }

            setDisplayHomeAsUpEnabled(hasBackStack)
            setDisplayShowHomeEnabled(hasBackStack)
        }
        invalidateOptionsMenu()

        if (hasBackStack) fab.hide() else fab.show()
    }
}

val app = 0