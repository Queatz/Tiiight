package com.queatz.tiiight.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.queatz.tiiight.App.Companion.app
import com.queatz.tiiight.R
import com.queatz.tiiight.managers.EmailManager
import com.queatz.tiiight.managers.SettingsManager
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsFragment : Fragment(), ShareableFragment {
    companion object {
        fun create() = SettingsFragment()
    }

    override fun onShare() {}
    override fun showUnarchive() = false
    override fun showArchive() = false
    override fun showShare() = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.activity_settings, container, false)!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sendFeedback.setOnClickListener { app<EmailManager>().sendFeedback() }
        viewArchive.setOnClickListener { (activity as MainActivity).showFragment(ArchivedNotesFragment.create(), getString(R.string.archived_notes)) }
        nightModeAlwaysSwitch.isChecked = app<SettingsManager>().settings.nightModeAlways
        nightModeAlwaysSwitch.setOnCheckedChangeListener { _, isChecked ->
            app<SettingsManager>().settings.apply {
                nightModeAlways = isChecked
                app<SettingsManager>().settings = this
            }
        }
    }

}