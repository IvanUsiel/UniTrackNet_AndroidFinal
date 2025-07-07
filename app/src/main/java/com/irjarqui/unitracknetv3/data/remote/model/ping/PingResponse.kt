package com.irjarqui.unitracknetv3.data.remote.model.ping

data class PingResponse(
    val resultado: Resultado
) {
    data class Resultado(
        val estado_ping: String,
        val estadisticas: Estadisticas,
        val salida_ping: String
    )

    data class Estadisticas(
        val paquetes_enviados: Int,
        val paquetes_recibidos: Int,
        val paquetes_perdidos: Int,
        val rtt_min_ms: Int,
        val rtt_avg_ms: Int,
        val rtt_max_ms: Int,
        val jitter_ms: Int
    )
}