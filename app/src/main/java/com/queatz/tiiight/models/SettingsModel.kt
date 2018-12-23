package com.queatz.tiiight.models

import io.objectbox.annotation.Entity
import java.util.*

@Entity
data class SettingsModel(
    var lastDate: Date = Date()
) : BaseModel()