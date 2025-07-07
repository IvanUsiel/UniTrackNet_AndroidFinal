package com.irjarqui.unitracknetv3.ui.telnet.model

data class TelnetResponseUi(
    val estado: String,
    val descripcion: String,
    val salida: String,
    val latenciaMs: Int?,
    val timestamp: String
)
