package com.queatz.tiiight.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.queatz.tiiight.R
import com.queatz.tiiight.managers.EmailManager
import com.queatz.tiiight.on
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsFragment : Fragment() {
    companion object {
        fun create() = SettingsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.activity_settings, container, false)!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sendFeedback.setOnClickListener { app.on(EmailManager::class).sendFeedback() }
        viewArchive.setOnClickListener { (activity as MainActivity).showFragment(ArchivedNotesFragment.create(), getString(R.string.archived_notes)) }
    }

}