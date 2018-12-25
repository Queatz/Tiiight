package com.queatz.tiiight

import android.app.Application
import com.queatz.tiiight.managers.AppManager
import com.queatz.tiiight.managers.ShortcutManager
import com.queatz.tiiight.views.app

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        app.on(AppManager::class).app = this
        app.on(ShortcutManager::class).make()
    }
}