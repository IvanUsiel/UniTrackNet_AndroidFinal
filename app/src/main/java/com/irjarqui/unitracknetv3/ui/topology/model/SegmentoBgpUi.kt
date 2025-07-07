package com.irjarqui.unitracknetv3.ui.topology.model

import com.irjarqui.unitracknetv3.data.remote.model.bgp.SegmentoBgpDto
import com.irjarqui.unitracknetv3.data.remote.model.bgp.VerificacionBgpDto

data class SegmentoBgpUi(val data: SegmentoBgpDto) : SegmentoUi {
    override val nombre: String get() = data.nombre
    override val verificaciones: List<VerificacionBgpDto> get() = data.verificaciones
}
