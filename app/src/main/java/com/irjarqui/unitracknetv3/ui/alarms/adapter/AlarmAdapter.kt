package com.irjarqui.unitracknetv3.ui.alarms.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.databinding.ItemAlarmBinding
import com.irjarqui.unitracknetv3.ui.alarms.model.AlarmStatus
import com.irjarqui.unitracknetv3.ui.alarms.model.AlarmUiModel

class AlarmAdapter(
    private val onClick: (AlarmUiModel) -> Unit
) : ListAdapter<AlarmUiModel, AlarmAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: AlarmUiModel) = with(binding) {
            val ctx = root.context
            tvSegmento.text = ctx.getString(R.string.segmento, alarm.segmento)
            tvPuntaA.text = ctx.getString(R.string.punta_a, alarm.hostA, alarm.ipA)
            tvPuntaB.text = ctx.getString(R.string.punta_b, alarm.hostB, alarm.ipB)
            tvProtocolo.text = ctx.getString(R.string.protocolo, alarm.protocolo)
            tvEstado.text = ctx.getString(R.string.estado, alarm.status.name)

            val colorRes = if (alarm.status == AlarmStatus.OK) R.color.green_theme else R.color.red
            val color = ContextCompat.getColor(root.context, colorRes)
            statusIndicator.setBackgroundColor(color)
            tvEstado.setTextColor(color)

            root.setOnClickListener { onClick(alarm) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AlarmUiModel>() {
            override fun areItemsTheSame(o: AlarmUiModel, n: AlarmUiModel) =
                o.segmento == n.segmento && o.hostA == n.hostA && o.hostB == n.hostB && o.protocolo == n.protocolo

            override fun areContentsTheSame(o: AlarmUiModel, n: AlarmUiModel) = o == n
        }
    }
}
