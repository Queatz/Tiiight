package com.queatz.tiiight

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_reminder_time_shortcut.view.*
import java.util.*



class ReminderTimeShortcutAdapter(private val onDate: (Date) -> Unit) : RecyclerView.Adapter<ReminderTimeShortcutAdapter.ViewHolder>() {

    var items = mutableListOf<ReminderTimeShortcutItem>()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ReminderTimeShortcutAdapter.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_reminder_time_shortcut, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, item.iconResId, 0, 0)

        for (drawable in holder.button.compoundDrawables) {
            drawable?.colorFilter = PorterDuffColorFilter(holder.button.resources.getColor(R.color.textHeader), PorterDuff.Mode.SRC_IN)
        }

        holder.button.text = item.text
        holder.button.setOnClickListener { onDate.invoke(item.date) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button = itemView.button!!
    }
}

data class ReminderTimeShortcutItem constructor(
    @DrawableRes val iconResId: Int,
    val text: String,
    val date: Date
)