package com.queatz.tiiight.managers

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import com.queatz.on.On
import com.queatz.tiiight.R
import com.queatz.tiiight.views.MainActivity
import java.util.*

class ShortcutManager constructor(private val on: On) {
    fun make() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return
        }

        val shortcutManager = on<AppManager>().app.getSystemService(ShortcutManager::class.java)

        val shortcuts = ArrayList<ShortcutInfo>()
        val shortcut = ShortcutInfo.Builder(on<AppManager>().app, "add")
            .setShortLabel(on<AppManager>().app.getString(R.string.add_reminder))
            .setIntent(Intent(on<AppManager>().app, MainActivity::class.java).setAction(Intent.ACTION_EDIT))
            .setIcon(Icon.createWithResource(on<AppManager>().app, R.drawable.ic_add_circle_pink_24dp))
            .build()

        shortcuts.add(0, shortcut)

        shortcutManager.dynamicShortcuts = shortcuts
    }
}