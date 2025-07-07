package com.irjarqui.unitracknetv3.data.remote.model.bgp

data class SegmentoBgpDto(
    val nombre: String,
    val verificaciones: List<VerificacionBgpDto>,
)
