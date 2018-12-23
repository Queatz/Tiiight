package com.queatz.tiiight

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.queatz.tiiight.models.ReminderModel
import kotlinx.android.synthetic.main.item_reminder.view.*

class ReminderAdapter(private val openCallback: (ReminderModel) -> Unit) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    var items = mutableListOf<ReminderModel>()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    field[oldItemPosition].objectBoxId == value[newItemPosition].objectBoxId
                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    field[oldItemPosition].text == value[newItemPosition].text
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
    }

    class ViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text = itemView.text!!
    }
}

class SwipeOptions constructor(private val adapter: ReminderAdapter,
                               private val resources: Resources,
                               private val doneCallback: (ReminderModel) -> Unit,
                               private val modifyCallback: (ReminderModel) -> Unit,
                               private val moveCallback: (reminder: ReminderModel, other: ReminderModel) -> Unit) :
    ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START or ItemTouchHelper.END) {

    private val paint = Paint()

    override fun onMove(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun onMoved(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
        val item = adapter.items[holder.adapterPosition]
        val other = adapter.items[target.adapterPosition]
        moveCallback.invoke(item, other)
        super.onMoved(recyclerView, holder, fromPos, target, toPos, x, y)
    }

    override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
        val item = adapter.items[holder.adapterPosition]
        when (direction) {
            ItemTouchHelper.START -> modifyCallback.invoke(item)
            ItemTouchHelper.END -> doneCallback.invoke(item)
        }
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val icon: Bitmap
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = holder.itemView
            val height = itemView.bottom.toFloat() - itemView.top.toFloat()
            val xOffset = resources.getDimensionPixelOffset(R.dimen.padDouble)

            if (dX > 0) {
                paint.color = resources.getColor(R.color.remove)
                val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                c.drawRect(background, paint)
                icon = resources.getDrawable(R.drawable.ic_check_black_24dp).toBitmap(resources.getColor(R.color.white))
                val iconRect = RectF(
                    itemView.left.toFloat() + xOffset,
                    itemView.top.toFloat() + height / 2 - icon.height / 2,
                    itemView.left.toFloat() + xOffset + icon.width,
                    itemView.top.toFloat() + height / 2 + icon.height / 2)
                c.drawBitmap(icon, null, iconRect, paint)
            } else {
                paint.color = resources.getColor(R.color.modify)
                val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                c.drawRect(background, paint)
                icon = resources.getDrawable(R.drawable.ic_edit_black_24dp).toBitmap(resources.getColor(R.color.white))
                val iconRect = RectF(
                    itemView.right.toFloat() - icon.width - xOffset,
                    itemView.top.toFloat() + height / 2 - icon.height / 2,
                    itemView.right.toFloat() - xOffset,
                    itemView.top.toFloat() + height / 2 + icon.height / 2)
                c.drawBitmap(icon, null, iconRect, paint)
            }
        }

        super.onChildDraw(c, recyclerView, holder, dX, dY, actionState, isCurrentlyActive)
    }
}