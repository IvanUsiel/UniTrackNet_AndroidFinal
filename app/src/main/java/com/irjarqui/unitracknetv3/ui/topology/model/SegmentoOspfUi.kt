package com.irjarqui.unitracknetv3.ui.topology.model

import com.irjarqui.unitracknetv3.data.remote.model.ospf.SegmentoOspfDto
import com.irjarqui.unitracknetv3.data.remote.model.ospf.VerificacionOspfDto

data class SegmentoOspfUi(val data: SegmentoOspfDto) : SegmentoUi {
    override val nombre: String get() = data.nombre
    override val verificaciones: List<VerificacionOspfDto> get() = data.verificaciones
}
