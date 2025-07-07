package com.irjarqui.unitracknetv3.data.remote.model.telnet


data class TelnetWrapperDto(
    val resultado: TelnetResponseDto
)

data class TelnetResponseDto(
    val estado_bgp: String? = null,
    val estado_ospf: String? = null,
    val descripcion: String,
    val origen_ip: String? = null,
    val vecino_ip: String? = null,
    val salida_telnet: String,
    val latencia_ms: Int? = null,
    val timestamp: String
)


