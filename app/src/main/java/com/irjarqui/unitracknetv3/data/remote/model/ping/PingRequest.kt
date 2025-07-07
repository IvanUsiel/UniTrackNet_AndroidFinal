package com.irjarqui.unitracknetv3.data.remote.model.ping

data class PingRequest(
    val origen: Nodo,
    val destino: Nodo,
    val parametros: Parametros = Parametros()
) {
    data class Nodo(val nombre: String, val ip: String)
    data class Parametros(
        val paquetes: Int = 20,
        val tamano_bytes: Int = 64,
        val timeout_ms: Int = 1000
    )
}