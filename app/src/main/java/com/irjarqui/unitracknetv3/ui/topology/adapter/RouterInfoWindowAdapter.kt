package com.irjarqui.unitracknetv3.ui.topology.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.irjarqui.unitracknetv3.R

class RouterInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    private val window: View = LayoutInflater.from(context).inflate(R.layout.item_info_window, null)

    private fun renderContents(marker: Marker, view: View) {
        val tvHostname = view.findViewById<TextView>(R.id.tvHostname)
        val tvIp = view.findViewById<TextView>(R.id.tvIp)
        val tvEstado = view.findViewById<TextView>(R.id.tvEstado)
        val tvTipo = view.findViewById<TextView>(R.id.tvTipo)

        tvHostname.text = marker.title

        val parts = marker.snippet?.split("|") ?: emptyList()

        if (parts.size == 3) {
            tvIp.text = parts.getOrNull(0)?.trim() ?: context.getString(R.string.ip__)
            tvEstado.text = parts.getOrNull(1)?.trim() ?: context.getString(R.string.estado__)
            tvTipo.text = parts.getOrNull(2)?.trim() ?: context.getString(R.string.tipo)
        } else {
            tvIp.text = marker.snippet ?: context.getString(R.string.ip)
            tvEstado.text = ""
            tvTipo.text = ""
        }
    }


    override fun getInfoWindow(marker: Marker): View {
        renderContents(marker, window)
        return window
    }

    override fun getInfoContents(marker: Marker): View? = null

}

