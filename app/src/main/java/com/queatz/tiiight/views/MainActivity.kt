package com.queatz.tiiight.views

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.queatz.tiiight.App.Companion.app
import com.queatz.tiiight.DoneReceiver.Companion.REMINDER_ID
import com.queatz.tiiight.R
import com.queatz.tiiight.managers.*
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.models.ReminderModel_
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private var remindersSubscription: DataSubscription? = null
    private var settingsButton: MenuItem? = null
    private var shareButton: MenuItem? = null
    private var unarchiveButton: MenuItem? = null
    private var archiveButton: MenuItem? = null
    private lateinit var adapter: ReminderAdapter
    private lateinit var filterAdapter: FilterAdapter

    private var currentFilter: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app<ContextManager>().context = this

        app<SettingsManager>().listener = {
            delegate.localNightMode =
                if (it.nightModeAlways) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { newReminder() }

        filterAdapter = FilterAdapter(true) { filterBy(it) }
        filters.adapter = filterAdapter
        filters.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        adapter = ReminderAdapter({ edit(it) }, resources)
        reminders.adapter = adapter
        reminders.layoutManager = LinearLayoutManager(this)

        ItemTouchHelper(SwipeOptions(adapter, resources, { reminder ->
            reminder.done = true
            reminder.doneDate = Date()
            app<DataManager>().box(ReminderModel::class).put(reminder)
            app<AlarmManager>().cancel(reminder)

            Snackbar.make(coordinator, getString(R.string.marked_done), Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo) {
                    reminder.done = false
                    app<DataManager>().box(ReminderModel::class).put(reminder)
                }
                .show()
        }, {
            edit(it, true)
        })).attachToRecyclerView(reminders)

        subscribe()

        intent?.let { onNewIntent(it) }
    }

    private fun subscribe() {
        remindersSubscription?.cancel()

        remindersSubscription = app<DataManager>().box(ReminderModel::class).query()
            .notEqual(ReminderModel_.text, "")
            .equal(ReminderModel_.done, false)
            .also {
                if (currentFilter.isNotBlank()) {
                    it.startsWith(ReminderModel_.text, currentFilter)
                }
            }
            .sort { o1, o2 -> o1.date.compareTo(o2.date) }
            .build()
            .subscribe()
            .on(AndroidScheduler.mainThread())
            .observer {
                adapter.items = it
                app<FilterManager>().getTopFilters(it).let {
                    val filters = it.toMutableList()
                    if (filters.isEmpty() && currentFilter.isNotBlank()) {
                        filters.add(currentFilter)
                    }

                    filterAdapter.items = filters
                }
            }
    }

    private fun filterBy(filter: String) {
        currentFilter = if (currentFilter == filter) {
            ""
        } else {
            filter
        }

        subscribe()
    }

    override fun onResume() {
        super.onResume()
        refreshUpButton()
    }

    override fun onDestroy() {
        remindersSubscription?.cancel()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                val reminderId = intent.getLongExtra(REMINDER_ID, -1)

                if (reminderId != -1L) {
                    app<DataManager>().box(ReminderModel::class).get(reminderId)?.let {
                        edit(it, true, clearBackStack = true)
                        app<NotificationManager>().dismiss(it)
                    }
                }
            } Intent.ACTION_EDIT -> {
                newReminder()
            }
            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    val reminder = ReminderModel(it, false, Date())
                    app<DataManager>().box(ReminderModel::class).put(reminder)
                    edit(reminder, clearBackStack = true)
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        refreshUpButton()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        settingsButton = menu.findItem(R.id.action_settings)
        shareButton = menu.findItem(R.id.action_share)
        unarchiveButton = menu.findItem(R.id.action_unarchive)
        archiveButton = menu.findItem(R.id.action_archive)
        shareButton?.isVisible = (getTopFragment() as? ShareableFragment)?.showShare() ?: true
        settingsButton?.isVisible = supportFragmentManager.backStackEntryCount <= 0
        unarchiveButton?.isVisible = (getTopFragment() as? ShareableFragment)?.showUnarchive() ?: false
        archiveButton?.isVisible = (getTopFragment() as? ShareableFragment)?.showArchive() ?: false

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
            R.id.action_share -> {
                (getTopFragment() as? ShareableFragment)?.onShare() ?: run {
                    onShare()
                }
                true
            }
            R.id.action_archive -> {
                (getTopFragment() as? ShareableFragment)?.onArchive()
                true
            }
            R.id.action_unarchive -> {
                (getTopFragment() as? ShareableFragment)?.onUnarchive()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onShare() {
        adapter.items.map { "[ ] ${it.text}" }.joinToString("\n").let {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_TEXT, it)
            startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.share_to)))
        }
    }

    private fun newReminder() {
        val reminder = ReminderModel(currentFilter.let { if (it.isNotBlank()) "$it " else it }, false, Date())
        app<DataManager>().box(ReminderModel::class).put(reminder)
        edit(reminder, isCreate = true)
    }

    private fun edit(reminder: ReminderModel, quickEdit: Boolean = false, clearBackStack: Boolean = false, isCreate: Boolean = false) {
        showFragment(EditReminderFragment.create(reminder.objectBoxId, quickEdit, isCreate), getString(
            if (isCreate) R.string.new_reminder else R.string.edit_reminder
        ), clearBackStack)
    }

    internal fun showFragment(fragment: Fragment, name: String? = null, clearBackStack: Boolean = false) {
        while (clearBackStack && supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        }

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
                getTopBackStackEntry()?.name ?: getString(R.string.app_name)
            } else {
                getString(R.string.app_name)
            }

            setDisplayHomeAsUpEnabled(hasBackStack)
            setDisplayShowHomeEnabled(hasBackStack)
        }
        invalidateOptionsMenu()

        if (hasBackStack) fab.hide() else fab.show()
    }

    private fun getTopBackStackEntry() = if (supportFragmentManager.backStackEntryCount > 0) supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1) else null
    private fun getTopFragment() = supportFragmentManager.fragments.lastOrNull()
}

interface ShareableFragment {
    fun onShare()
    fun onArchive() {}
    fun onUnarchive() {}
    fun showUnarchive(): Boolean
    fun showArchive(): Boolean
    fun showShare(): Boolean
}

data class FilterCount(val name: String, val count: Int)