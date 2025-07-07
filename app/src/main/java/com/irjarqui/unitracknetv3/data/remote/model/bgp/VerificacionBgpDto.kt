package com.irjarqui.unitracknetv3.data.remote.model.bgp

data class VerificacionBgpDto(
    val origen: NodoBgpDto,
    val vecino: NodoBgpDto,
    val estado_bgp: String
)
