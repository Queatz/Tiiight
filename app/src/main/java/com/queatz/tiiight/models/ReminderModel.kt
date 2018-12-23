package com.queatz.tiiight.models

import io.objectbox.annotation.Entity
import java.util.*

@Entity
data class ReminderModel(
    var text: String = "",
    var done: Boolean = false,
    var date: Date = Date()
) : BaseModel()