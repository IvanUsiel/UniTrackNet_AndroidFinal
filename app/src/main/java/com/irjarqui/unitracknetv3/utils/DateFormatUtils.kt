package com.irjarqui.unitracknetv3.utils

import java.text.SimpleDateFormat
import java.util.*

object DateFormatUtils {

    fun formatFechaHoyODiferencia(fechaTexto: String?): String? {
        if (fechaTexto.isNullOrBlank()) return null

        return try {
            val formatoEntrada = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val fecha = formatoEntrada.parse(fechaTexto) ?: return null

            val calendarHoy = Calendar.getInstance()
            val calendarFecha = Calendar.getInstance().apply { time = fecha }

            val esMismoDia = calendarHoy.get(Calendar.YEAR) == calendarFecha.get(Calendar.YEAR) &&
                    calendarHoy.get(Calendar.DAY_OF_YEAR) == calendarFecha.get(Calendar.DAY_OF_YEAR)

            if (esMismoDia) {
                val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(fecha)
                "Hoy a las $hora"
            } else {
                val diasDiferencia =
                    ((calendarHoy.timeInMillis - calendarFecha.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                "desactualizado:$diasDiferencia"
            }

        } catch (e: Exception) {
            null
        }
    }
}
