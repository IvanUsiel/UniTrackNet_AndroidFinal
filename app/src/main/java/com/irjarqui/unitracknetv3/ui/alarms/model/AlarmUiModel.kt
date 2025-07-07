package com.irjarqui.unitracknetv3.ui.alarms.model

data class AlarmUiModel(
    val segmento: String,
    val protocolo: String,
    val hostA: String,
    val ipA: String,
    val hostB: String,
    val ipB: String,
    val status: AlarmStatus
)

enum class AlarmStatus { OK, ERROR }
