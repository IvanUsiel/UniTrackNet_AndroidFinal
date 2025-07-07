package com.irjarqui.unitracknetv3.ui.topology.view

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.data.remote.model.bgp.VerificacionBgpDto
import com.irjarqui.unitracknetv3.data.remote.model.ospf.VerificacionOspfDto
import com.irjarqui.unitracknetv3.ui.telnet.model.NodoUiModel
import com.irjarqui.unitracknetv3.ui.topology.adapter.RouterInfoWindowAdapter
import com.irjarqui.unitracknetv3.ui.topology.model.SegmentUiModel
import com.irjarqui.unitracknetv3.ui.telnet.model.SegmentoUi

class TopologyMapFragment : SupportMapFragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val trafficMarkers = mutableListOf<Marker>()
    private val trafficHandlers = mutableListOf<Handler>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMapAsync(this)
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setInfoWindowAdapter(RouterInfoWindowAdapter(requireContext()))

        map.setOnPolylineClickListener { polyline ->
            val segmento = polyline.tag as? SegmentoUi ?: return@setOnPolylineClickListener
            abrirTelnetFragment(segmento)
        }



        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_dark)
            )
            if (!success) Log.e("MapStyle", "Fallo al aplicar el estilo.")
        } catch (e: Resources.NotFoundException) {
            Log.e("MapStyle", "Archivo de estilo no encontrado.", e)
        }

        val bounds = LatLngBounds(LatLng(5.0, -130.0), LatLng(55.0, -60.0))
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

        (parentFragment as? TopologyFragment)?.onMapaListo()
    }

    fun clearMap() {
        if (::map.isInitialized) {
            map.clear()

            trafficMarkers.forEach { it.remove() }
            trafficMarkers.clear()

            trafficHandlers.forEach { it.removeCallbacksAndMessages(null) }
            trafficHandlers.clear()
        }
    }

    fun drawSegmentosCombinados(
        ospf: List<VerificacionOspfDto>,
        bgp: List<VerificacionBgpDto>
    ) {
        if (!::map.isInitialized) return
        clearMap()

        val markerOk = getResizedBitmapDescriptor(R.drawable.router_ok, 64, 64)
        val markerFail = getResizedBitmapDescriptor(R.drawable.router_fail, 64, 64)
        val boundsBuilder = LatLngBounds.Builder()

        data class NodoUi(
            val latLng: LatLng,
            val nombre: String,
            val ip: String,
            val isOk: Boolean,
            val tipo: String
        )

        val nodosUnicos = mutableSetOf<NodoUi>()

        ospf.forEach { v ->
            val origen = LatLng(v.origen.lat, v.origen.lon)
            val vecino = LatLng(v.vecino.lat, v.vecino.lon)
            val isOk = v.estado_ospf == "ok"

            nodosUnicos += NodoUi(origen, v.origen.nombre, v.origen.ip, isOk, "OSPF")
            nodosUnicos += NodoUi(vecino, v.vecino.nombre, v.vecino.ip, isOk, "OSPF")


            val polyline = map.addPolyline(
                PolylineOptions().add(origen, vecino).color(
                    if (isOk) 0xFF4CAF50.toInt() else 0xFFF44336.toInt()
                ).width(6f)
                    .clickable(true)
            )

            polyline.tag = SegmentoUi(
                nodoA = toNodoUiModel(v.origen.nombre, v.origen.ip),
                nodoB = toNodoUiModel(v.vecino.nombre, v.vecino.ip),
                protocolo = "OSPF"
            )


            boundsBuilder.include(origen)
            boundsBuilder.include(vecino)
        }

        bgp.forEach { v ->
            val origen = LatLng(v.origen.lat, v.origen.lon)
            val vecino = LatLng(v.vecino.lat, v.vecino.lon)
            val isOk = v.estado_bgp == "ok"

            nodosUnicos += NodoUi(origen, v.origen.nombre, v.origen.ip, isOk, "BGP")
            nodosUnicos += NodoUi(vecino, v.vecino.nombre, v.vecino.ip, isOk, "BGP")

            val polyline = map.addPolyline(
                PolylineOptions().add(origen, vecino).color(
                    if (isOk) 0xFF2196F3.toInt() else 0xFFF44336.toInt()
                ).width(6f)
                    .clickable(true)
            )

            polyline.tag = SegmentoUi(
                nodoA = toNodoUiModel(v.origen.nombre, v.origen.ip),
                nodoB = toNodoUiModel(v.vecino.nombre, v.vecino.ip),
                protocolo = "BGP"
            )


            boundsBuilder.include(origen)
            boundsBuilder.include(vecino)
        }

        nodosUnicos.forEach { nodo ->
            val icon = if (nodo.isOk) markerOk else markerFail
            val estado = if (nodo.isOk) "OK" else "ERROR"

            map.addMarker(
                MarkerOptions()
                    .position(nodo.latLng)
                    .title(nodo.nombre)
                    .snippet("IP: ${nodo.ip} | Estado: $estado | Tipo: ${nodo.tipo}")
                    .icon(icon)
            )
        }

        try {
            val bounds = boundsBuilder.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (e: Exception) {
            Log.e("MapZoom", "Zoom combinado falló", e)
        }
    }


    fun drawSegmentosCombinadosGlobal(
        segmentos: List<SegmentUiModel>,
        showOspf: Boolean,
        showBgp: Boolean
    ) {
        if (!::map.isInitialized) return
        clearMap()

        val markerOk = getResizedBitmapDescriptor(R.drawable.router_ok, 64, 64)
        val markerFail = getResizedBitmapDescriptor(R.drawable.router_fail, 64, 64)
        val boundsBuilder = LatLngBounds.Builder()

        data class NodoUi(
            val latLng: LatLng,
            val nombre: String,
            val ip: String,
            val estados: MutableList<Pair<String, String>>
        )

        val nodosMap = mutableMapOf<String, NodoUi>()

        fun agregarNodo(ip: String, latLng: LatLng, nombre: String, protocolo: String, estado: String) {
            val nodo = nodosMap[ip]
            if (nodo == null) {
                nodosMap[ip] = NodoUi(latLng, nombre, ip, mutableListOf(protocolo to estado))
            } else {
                if (nodo.estados.none { it.first == protocolo }) {
                    nodo.estados.add(protocolo to estado)
                }
            }
        }

        segmentos.forEach { segmento ->
            if (showOspf) {
                segmento.verificacionesOspf.forEach { v ->
                    val origen = LatLng(v.origen.lat, v.origen.lon)
                    val vecino = LatLng(v.vecino.lat, v.vecino.lon)
                    val isOk = v.estado_ospf == "ok"

                    agregarNodo(v.origen.ip, origen, v.origen.nombre, "OSPF", v.estado_ospf)
                    agregarNodo(v.vecino.ip, vecino, v.vecino.nombre, "OSPF", v.estado_ospf)

                    val polyline = map.addPolyline(
                        PolylineOptions().add(origen, vecino).color(
                            if (isOk) 0xFF4CAF50.toInt() else 0xFFF44336.toInt()
                        ).width(6f)
                            .clickable(true)
                    )

                    polyline.tag = SegmentoUi(
                        nodoA = NodoUiModel(v.origen.nombre, v.origen.ip),
                        nodoB = NodoUiModel(v.vecino.nombre, v.vecino.ip),
                        protocolo = "OSPF"
                    )

                    boundsBuilder.include(origen)
                    boundsBuilder.include(vecino)
                }
            }

            if (showBgp) {
                segmento.verificacionesBgp.forEach { v ->
                    val origen = LatLng(v.origen.lat, v.origen.lon)
                    val vecino = LatLng(v.vecino.lat, v.vecino.lon)
                    val isOk = v.estado_bgp == "ok"

                    agregarNodo(v.origen.ip, origen, v.origen.nombre, "BGP", v.estado_bgp)
                    agregarNodo(v.vecino.ip, vecino, v.vecino.nombre, "BGP", v.estado_bgp)

                    val polyline = map.addPolyline(
                        PolylineOptions().add(origen, vecino).color(
                            if (isOk) 0xFF2196F3.toInt() else 0xFFF44336.toInt()
                        ).width(6f)
                            .clickable(true)
                    )

                    polyline.tag = SegmentoUi(
                        nodoA = NodoUiModel(v.origen.nombre, v.origen.ip),
                        nodoB = NodoUiModel(v.vecino.nombre, v.vecino.ip),
                        protocolo = "BGP"
                    )

                    boundsBuilder.include(origen)
                    boundsBuilder.include(vecino)
                }
            }
        }

        nodosMap.values.forEach { nodo ->
            val hayError = nodo.estados.any { it.second.equals("error", ignoreCase = true) }
            val icon = if (hayError) markerFail else markerOk

            val snippetText = nodo.estados.joinToString(separator = "\n", prefix = "IP: ${nodo.ip}\n") { (proto, estado) ->
                "$proto: ${estado.uppercase()}"
            }

            map.addMarker(
                MarkerOptions()
                    .position(nodo.latLng)
                    .title(nodo.nombre)
                    .snippet(snippetText)
                    .icon(icon)
            )
        }

        try {
            val bounds = boundsBuilder.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (e: Exception) {
            Log.e("MapZoom", "Zoom combinado global falló", e)
        }
    }

    private fun getResizedBitmapDescriptor(resourceId: Int, width: Int, height: Int): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(resources, resourceId)
        val scaled = Bitmap.createScaledBitmap(bitmap, width, height, false)
        return BitmapDescriptorFactory.fromBitmap(scaled)
    }

    private fun abrirTelnetFragment(segmento: SegmentoUi) {
        val bundle = Bundle().apply {
            putString("origenHost",  segmento.nodoA.hostname)
            putString("origenIp",    segmento.nodoA.ip)
            putString("destinoHost", segmento.nodoB.hostname)
            putString("destinoIp",   segmento.nodoB.ip)
            putString("protocolo",   segmento.protocolo)
        }
        (requireActivity() as? TopologyStatusActivity)?.showTelnetOverlay(bundle)
    }



    private fun toNodoUiModel(nombre: String, ip: String): NodoUiModel {
        return NodoUiModel(
            hostname = nombre,
            ip = ip
        )
    }
}