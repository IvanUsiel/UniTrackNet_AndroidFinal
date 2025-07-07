package com.irjarqui.unitracknetv3.ui.telnet.model

data class NodoUiModel(
    val hostname: String,
    val ip: String
)

data class SegmentoUi(
    val nodoA: NodoUiModel,
    val nodoB: NodoUiModel,
    val protocolo: String
)
