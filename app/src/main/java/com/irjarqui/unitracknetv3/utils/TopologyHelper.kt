package com.irjarqui.unitracknetv3.utils

import com.irjarqui.unitracknetv3.data.remote.model.bgp.SegmentoBgpDto
import com.irjarqui.unitracknetv3.data.remote.model.ospf.SegmentoOspfDto
import com.irjarqui.unitracknetv3.ui.alarms.model.AlarmStatus
import com.irjarqui.unitracknetv3.ui.alarms.model.AlarmUiModel
import com.irjarqui.unitracknetv3.ui.topology.model.SegmentUiModel

object TopologyHelper {

    fun combinarSegmentos(
        ospf: List<SegmentoOspfDto>,
        bgp: List<SegmentoBgpDto>
    ): List<SegmentUiModel> {
        val mapa = mutableMapOf<String, SegmentUiModel>()

        ospf.forEach { s ->
            mapa[s.nombre] = SegmentUiModel(
                nombre = s.nombre,
                verificacionesOspf = s.verificaciones,
                verificacionesBgp = emptyList()
            )
        }

        bgp.forEach { s ->
            val existente = mapa[s.nombre]
            if (existente != null) {
                mapa[s.nombre] = existente.copy(
                    verificacionesBgp = s.verificaciones
                )
            } else {
                mapa[s.nombre] = SegmentUiModel(
                    nombre = s.nombre,
                    verificacionesOspf = emptyList(),
                    verificacionesBgp = s.verificaciones
                )
            }
        }

        return mapa.values.toList()
    }

    fun buildAlarmList(
        bgpList: List<SegmentoBgpDto>,
        ospfList: List<SegmentoOspfDto>
    ): List<AlarmUiModel> {

        val alarms = mutableListOf<AlarmUiModel>()

        bgpList.forEach { seg ->
            seg.verificaciones.forEach { v ->
                alarms += AlarmUiModel(
                    segmento = seg.nombre,
                    protocolo = "BGP",
                    hostA = v.origen.nombre,
                    ipA = v.origen.ip,
                    hostB = v.vecino.nombre,
                    ipB = v.vecino.ip,
                    status = if (v.estado_bgp.equals(
                            "ok",
                            true
                        )
                    ) AlarmStatus.OK else AlarmStatus.ERROR
                )
            }
        }

        ospfList.forEach { seg ->
            seg.verificaciones.forEach { v ->
                alarms += AlarmUiModel(
                    segmento = seg.nombre,
                    protocolo = "OSPF",
                    hostA = v.origen.nombre,
                    ipA = v.origen.ip,
                    hostB = v.vecino.nombre,
                    ipB = v.vecino.ip,
                    status = if (v.estado_ospf.equals(
                            "ok",
                            true
                        )
                    ) AlarmStatus.OK else AlarmStatus.ERROR
                )
            }
        }

        return alarms.sortedWith(
            compareBy<AlarmUiModel> { it.status }
                .thenBy { it.segmento }
                .thenBy { it.protocolo }
        )
    }
}
