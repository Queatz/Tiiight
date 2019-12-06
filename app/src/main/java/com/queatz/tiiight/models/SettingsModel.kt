package com.queatz.tiiight.models

import io.objectbox.annotation.Entity
import java.util.*

@Entity
data class SettingsModel constructor(
    var lastDate: Date = Date(),
    var nightModeAlways: Boolean = false
) : BaseModel()