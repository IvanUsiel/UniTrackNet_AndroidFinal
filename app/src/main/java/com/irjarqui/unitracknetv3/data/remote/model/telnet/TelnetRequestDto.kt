package com.irjarqui.unitracknetv3.data.remote.model.telnet

data class TelnetRequestDto(
    val origen: NodoDto,
    val vecino: NodoDto
)

data class NodoDto(
    val nombre: String,
    val ip: String
)

