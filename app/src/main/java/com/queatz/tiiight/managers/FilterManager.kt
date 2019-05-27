package com.queatz.tiiight.managers

import com.queatz.on.On
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.models.ReminderModel_
import com.queatz.tiiight.views.FilterCount

class FilterManager constructor(private val on: On) {
    fun getTopFilters(currentFilter: String? = null): List<String> {
        return getTopFilters(
            on<DataManager>().box(ReminderModel::class).query()
                .notEqual(ReminderModel_.text, "")
                .equal(ReminderModel_.done, false)
                .also {
                    if (currentFilter?.isNotBlank() == true) {
                        it.startsWith(ReminderModel_.text, currentFilter)
                    }
                }
                .sort { o1, o2 -> o1.date.compareTo(o2.date) }
                .build()
                .find())
    }

    fun getTopFilters(reminders: List<ReminderModel>): List<String> {
        val filters = mutableSetOf<String>()

        reminders.map {
            it.text.split(Regex("\\s+"), 2)[0]
        }.groupBy { it }.map {
            FilterCount(it.key, it.value.size)
        }.sortedByDescending {
            it.count
        }.map {
            it.name
        }.let {
            filters.addAll(it)
        }

        return filters.toList()
    }
}