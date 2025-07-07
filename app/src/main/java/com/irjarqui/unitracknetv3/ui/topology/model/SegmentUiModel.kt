package com.irjarqui.unitracknetv3.ui.topology.model

import com.irjarqui.unitracknetv3.data.remote.model.bgp.VerificacionBgpDto
import com.irjarqui.unitracknetv3.data.remote.model.ospf.VerificacionOspfDto

data class SegmentUiModel(
    val nombre: String,
    val verificacionesOspf: List<VerificacionOspfDto>,
    val verificacionesBgp: List<VerificacionBgpDto>
)
