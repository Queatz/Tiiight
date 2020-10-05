package com.queatz.tiiight.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.queatz.tiiight.R
import kotlinx.android.synthetic.main.item_filter.view.*

class FilterAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<FilterViewHolder>() {

    var items = mutableListOf<String>()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    field[oldItemPosition] == value[newItemPosition]
                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true
            })
            items.clear()
            items.addAll(value)
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        FilterViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_filter, parent, false)
        )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = items[position]

        holder.itemView.filterName.text = filter

        holder.itemView.setOnClickListener {
            holder.selected = !holder.selected
            holder.itemView.filterName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, if (holder.selected) R.drawable.ic_baseline_close_24 else 0, 0)
            onClick.invoke(filter)
        }
    }
}

class FilterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var selected = false
}