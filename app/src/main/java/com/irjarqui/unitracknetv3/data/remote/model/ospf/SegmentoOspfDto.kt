package com.irjarqui.unitracknetv3.data.remote.model.ospf

data class SegmentoOspfDto(
    val nombre: String,
    val verificaciones: List<VerificacionOspfDto>,
)
