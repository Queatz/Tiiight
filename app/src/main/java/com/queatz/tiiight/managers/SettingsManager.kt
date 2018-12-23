package com.queatz.tiiight.managers

import com.queatz.tiiight.PoolMember
import com.queatz.tiiight.models.SettingsModel

class SettingsManager : PoolMember() {
    var settings: SettingsModel
        get() = on(DataManager::class).box(SettingsModel::class).all.firstOrNull() ?: SettingsModel()
        set(value) {
            on(DataManager::class).box(SettingsModel::class).put(value)
        }
}
