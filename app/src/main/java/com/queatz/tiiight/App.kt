package com.queatz.tiiight

import androidx.multidex.MultiDexApplication
import com.queatz.on.On
import com.queatz.tiiight.managers.AppManager
import com.queatz.tiiight.managers.ShortcutManager

class App : MultiDexApplication() {

    companion object {
        val app = On()
    }

    override fun onCreate() {
        super.onCreate()
        app<AppManager>().app = this
        app<ShortcutManager>().make()
    }
}