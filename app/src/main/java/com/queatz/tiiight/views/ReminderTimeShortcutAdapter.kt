package com.queatz.tiiight.views

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.queatz.tiiight.R
import kotlinx.android.synthetic.main.item_reminder_time_shortcut.view.*
import java.text.SimpleDateFormat
import java.util.*



class ReminderTimeShortcutAdapter(private val onDate: (Date) -> Unit) : RecyclerView.Adapter<ReminderTimeShortcutAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("EE, MMM dd, h:mma", Locale.US)

    var items = mutableListOf<ReminderTimeShortcutItem>()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_reminder_time_shortcut,
                parent,
                false
            )
        )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, item.iconResId, 0, 0)

        for (drawable in holder.button.compoundDrawables) {
            drawable?.colorFilter = PorterDuffColorFilter(holder.button.resources.getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN)
        }

        holder.button.text = item.text
        holder.button.setOnClickListener { onDate.invoke(item.date) }
        holder.actualTime.text = dateFormat.format(item.date)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button = itemView.button!!
        val actualTime = itemView.actualTime!!
    }
}

data class ReminderTimeShortcutItem constructor(
    @DrawableRes val iconResId: Int,
    val text: String,
    val date: Date
)