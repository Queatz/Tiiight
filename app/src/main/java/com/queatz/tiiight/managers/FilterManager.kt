package com.queatz.tiiight.managers

import com.queatz.tiiight.PoolMember
import com.queatz.tiiight.models.ReminderModel
import com.queatz.tiiight.models.ReminderModel_
import com.queatz.tiiight.on
import com.queatz.tiiight.views.FilterCount
import com.queatz.tiiight.views.app

class FilterManager : PoolMember() {
    fun getTopFilters(currentFilter: String? = null): List<String> {
        return getTopFilters(
            app.on(DataManager::class).box(ReminderModel::class).query()
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