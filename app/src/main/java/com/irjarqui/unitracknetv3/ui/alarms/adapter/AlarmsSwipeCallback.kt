package com.irjarqui.unitracknetv3.ui.alarms.adapter

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.ui.alarms.model.AlarmUiModel

class AlarmsSwipeCallback(
    private val onTelnet: (AlarmUiModel, Int) -> Unit,
    private val onPing: (AlarmUiModel, Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onChildDraw(
        c: Canvas,
        rv: RecyclerView,
        vh: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val item = vh.itemView
        val ctx = rv.context

        when {
            dX < 0 -> {
                val bgPaint = Paint().apply {
                    color = ContextCompat.getColor(rv.context, R.color.green_theme)
                }
                c.drawRect(
                    item.right + dX,
                    item.top.toFloat(),
                    item.right.toFloat(),
                    item.bottom.toFloat(),
                    bgPaint
                )

                val text = ctx.getString(R.string.telnet)
                val paintTxt = Paint().apply {
                    color = ContextCompat.getColor(rv.context, android.R.color.black)
                    textSize = 42f
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val x = item.right - paintTxt.measureText(text) - 60f
                val y = item.top + item.height / 2f + paintTxt.textSize / 2f
                c.drawText(text, x, y, paintTxt)
            }

            dX > 0 -> {
                val bgPaint = Paint().apply {
                    color = ContextCompat.getColor(rv.context, R.color.blue_theme)
                }
                c.drawRect(
                    item.left.toFloat(),
                    item.top.toFloat(),
                    item.left + dX,
                    item.bottom.toFloat(),
                    bgPaint
                )

                val text = ctx.getString(R.string.ping)
                val paintTxt = Paint().apply {
                    color = ContextCompat.getColor(rv.context, android.R.color.black)
                    textSize = 42f
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val x = item.left + 60f
                val y = item.top + item.height / 2f + paintTxt.textSize / 2f
                c.drawText(text, x, y, paintTxt)
            }
        }

        super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onMove(
        rv: RecyclerView,
        vh: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
        val pos = vh.adapterPosition
        if (pos == RecyclerView.NO_POSITION) return

        val rv = vh.itemView.parent as? RecyclerView ?: return
        val adapter = rv.adapter as? AlarmAdapter ?: return
        val alarm = adapter.currentList.getOrNull(pos) ?: return

        if (dir == ItemTouchHelper.LEFT) onTelnet(alarm, pos) else onPing(alarm, pos)
    }
}
