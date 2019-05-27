package com.queatz.tiiight.managers

import com.queatz.on.On
import com.queatz.tiiight.models.SettingsModel

class SettingsManager constructor(private val on: On) {
    var settings: SettingsModel
        get() = on<DataManager>().box(SettingsModel::class).all.firstOrNull() ?: SettingsModel()
        set(value) {
            on<DataManager>().box(SettingsModel::class).put(value)
        }
}
