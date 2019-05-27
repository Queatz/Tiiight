package com.queatz.tiiight

import android.app.Application
import com.queatz.on.On
import com.queatz.tiiight.managers.AppManager
import com.queatz.tiiight.managers.ShortcutManager

class App : Application() {

    companion object {
        val app = On()
    }

    override fun onCreate() {
        super.onCreate()
        app<AppManager>().app = this
        app<ShortcutManager>().make()
    }
}