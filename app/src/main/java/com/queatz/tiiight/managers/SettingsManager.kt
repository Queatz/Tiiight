package com.queatz.tiiight.managers

import com.queatz.on.On
import com.queatz.tiiight.models.SettingsModel

class SettingsManager constructor(private val on: On) {

    var listener: ((SettingsModel) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(settings)
        }

    var settings: SettingsModel
        get() = on<DataManager>().box(SettingsModel::class).all.firstOrNull() ?: SettingsModel()
        set(value) {
            listener?.invoke(value)
            on<DataManager>().box(SettingsModel::class).put(value)
        }
}
