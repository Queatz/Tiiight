package com.queatz.tiiight

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.queatz.tiiight.models.ReminderModel
import kotlinx.android.synthetic.main.item_reminder.view.*
import java.util.*

class ReminderAdapter(private val openCallback: (ReminderModel) -> Unit, private val resources: Resources) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    var items = mutableListOf<ReminderModel>()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    field[oldItemPosition].objectBoxId == value[newItemPosition].objectBoxId
                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    field[oldItemPosition].text == value[newItemPosition].text &&
                            getSectionHeader(field[oldItemPosition]) == getSectionHeader(value[newItemPosition]) &&
                            showSectionHeader(field, oldItemPosition) == showSectionHeader(value, newItemPosition)
            })
            items.clear()
            items.addAll(value)
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ReminderAdapter.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ReminderAdapter.ViewHolder, position: Int) {
        val reminder = items[position]
        holder.text.text = reminder.text
        holder.itemView.setOnClickListener { openCallback.invoke(reminder) }

        val header = getSectionHeader(reminder)

        if (showSectionHeader(items, position)) {
            holder.sectionHeader.visibility = View.GONE
        } else {
            holder.sectionHeader.visibility = View.VISIBLE
            holder.sectionHeader.text = header
        }

    }

    private fun showSectionHeader(items: List<ReminderModel>, position: Int): Boolean {
        val previousHeader = if (position > 0) getSectionHeader(items[position - 1]) else null
        val header = getSectionHeader(items[position])

        return previousHeader == header
    }

    private fun getSectionHeader(reminder: ReminderModel): String {
        val now = Date()

        if (now.after(reminder.date)) {
            return resources.getString(R.string.today)
        }

        if (DateUtils.isToday(reminder.date.time)) {
            return resources.getString(R.string.later_today)
        }

        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = now
        cal2.time = reminder.date
        val isSameWeek = cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)

        return if (isSameWeek) {
            resources.getString(R.string.this_week)
        } else {
            resources.getString(R.string.later)
        }


    }

    class ViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text = itemView.text!!
        val sectionHeader = itemView.sectionHeader!!
    }
}

class SwipeOptions constructor(private val adapter: ReminderAdapter,
                               private val resources: Resources,
                               private val doneCallback: (ReminderModel) -> Unit,
                               private val modifyCallback: (ReminderModel) -> Unit,
                               private val moveCallback: (reminder: ReminderModel, other: ReminderModel) -> Unit) :
        ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START or ItemTouchHelper.END) {

    private val paint = Paint()

    override fun onMove(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = true

    override fun onMoved(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
        val item = adapter.items[holder.adapterPosition]
        val other = adapter.items[target.adapterPosition]
        moveCallback.invoke(item, other)
        super.onMoved(recyclerView, holder, fromPos, target, toPos, x, y)
    }

    override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
        val item = adapter.items[holder.adapterPosition]
        when (direction) {
            ItemTouchHelper.START -> {
                modifyCallback.invoke(item)
                adapter.notifyItemChanged(holder.adapterPosition)
            }
            ItemTouchHelper.END -> doneCallback.invoke(item)
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            getDefaultUIUtil().onSelected(viewHolder.itemView.text)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        getDefaultUIUtil().clearView(viewHolder.itemView.text)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val icon: Bitmap
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = holder.itemView.text
            val viewRect = RectF(itemView.left.toFloat() + holder.itemView.left,
                itemView.top.toFloat() + holder.itemView.top,
                itemView.right.toFloat() + holder.itemView.left,
                itemView.bottom.toFloat() + holder.itemView.top)
            val height = viewRect.bottom - viewRect.top
            val xOffset = resources.getDimensionPixelOffset(R.dimen.padDouble)

            if (dX > 0) {
                paint.color = resources.getColor(R.color.remove)
                val background = RectF(viewRect.left, viewRect.top, dX, viewRect.bottom)
                c.drawRect(background, paint)
                icon = resources.getDrawable(R.drawable.ic_check_black_24dp).toBitmap(resources.getColor(R.color.white))
                val iconRect = RectF(
                    viewRect.left + xOffset,
                    viewRect.top + height / 2 - icon.height / 2,
                    viewRect.left + xOffset + icon.width,
                    viewRect.top + height / 2 + icon.height / 2)
                c.drawBitmap(icon, null, iconRect, paint)
            } else {
                paint.color = resources.getColor(R.color.modify)
                val background = RectF(viewRect.right + dX, viewRect.top, viewRect.right, viewRect.bottom)
                c.drawRect(background, paint)
                icon = resources.getDrawable(R.drawable.ic_edit_black_24dp).toBitmap(resources.getColor(R.color.white))
                val iconRect = RectF(
                    viewRect.right - icon.width - xOffset,
                    viewRect.top + height / 2 - icon.height / 2,
                    viewRect.right - xOffset,
                    viewRect.top + height / 2 + icon.height / 2)
                c.drawBitmap(icon, null, iconRect, paint)
            }
        }

        getDefaultUIUtil().onDraw(c, recyclerView, holder.itemView.text, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        getDefaultUIUtil().onDrawOver(c, recyclerView, viewHolder.itemView.text, dX, dY, actionState, isCurrentlyActive)
    }
}