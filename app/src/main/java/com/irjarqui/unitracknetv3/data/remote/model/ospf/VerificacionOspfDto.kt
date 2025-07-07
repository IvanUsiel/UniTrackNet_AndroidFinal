package com.irjarqui.unitracknetv3.data.remote.model.ospf

data class VerificacionOspfDto(
    val origen: NodoOspfDto,
    val vecino: NodoOspfDto,
    val estado_ospf: String
)
