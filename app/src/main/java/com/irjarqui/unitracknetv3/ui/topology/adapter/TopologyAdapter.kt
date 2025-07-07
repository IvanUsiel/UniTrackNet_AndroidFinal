package com.irjarqui.unitracknetv3.ui.topology.adapter

import android.view.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.databinding.ItemOspfSegmentBinding
import com.irjarqui.unitracknetv3.ui.topology.model.SegmentUiModel


class TopologyAdapter(
    private val onClick: (SegmentUiModel) -> Unit,
    var showOspf: Boolean,
    var showBgp: Boolean
) : RecyclerView.Adapter<TopologyAdapter.ViewHolder>() {

    private var segmentos = listOf<SegmentUiModel>()

    fun submitList(list: List<SegmentUiModel>) {
        segmentos = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemOspfSegmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(segmento: SegmentUiModel) {
            binding.tvHostname.text = segmento.nombre

            val totalLinks = when {
                showOspf && showBgp -> segmento.verificacionesOspf.size + segmento.verificacionesBgp.size
                showOspf -> segmento.verificacionesOspf.size
                showBgp -> segmento.verificacionesBgp.size
                else -> 0
            }
            binding.tvIpAddress.text = "$totalLinks enlaces"

            val hayErrorOspf = segmento.verificacionesOspf.any { it.estado_ospf != "ok" }
            val hayErrorBgp = segmento.verificacionesBgp.any { it.estado_bgp != "ok" }

            val hayAlarma = when {
                showOspf && showBgp -> hayErrorOspf || hayErrorBgp
                showOspf -> hayErrorOspf
                showBgp -> hayErrorBgp
                else -> false
            }

            val context = binding.root.context
            val strokeColor = when {
                showOspf && showBgp -> if (hayErrorOspf || hayErrorBgp)
                    ContextCompat.getColor(context, R.color.red)
                else
                    ContextCompat.getColor(context, R.color.green_theme)

                showOspf -> if (hayErrorOspf)
                    ContextCompat.getColor(context, R.color.red)
                else
                    ContextCompat.getColor(context, R.color.green_theme)

                showBgp -> if (hayErrorBgp)
                    ContextCompat.getColor(context, R.color.red)
                else
                    ContextCompat.getColor(context, R.color.green_theme)

                else -> ContextCompat.getColor(context, R.color.gray)
            }
            binding.cardNode.strokeColor = strokeColor

            binding.cardNode.setOnClickListener {
                onClick(segmento)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOspfSegmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = segmentos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(segmentos[position])
    }
}
