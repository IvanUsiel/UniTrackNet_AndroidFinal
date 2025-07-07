package com.irjarqui.unitracknetv3.ui.telnet.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.application.UniTrackNetApp
import com.irjarqui.unitracknetv3.data.remote.model.telnet.NodoDto
import com.irjarqui.unitracknetv3.data.remote.model.telnet.TelnetRequestDto
import com.irjarqui.unitracknetv3.databinding.FragmentTelnetConsoleBinding
import kotlinx.coroutines.launch
import java.lang.Exception

class TelnetConsoleFragment : Fragment() {

    private var _binding: FragmentTelnetConsoleBinding? = null
    private val binding get() = _binding!!

    private val telnetRepo by lazy {
        (requireActivity().application as UniTrackNetApp).telnetRepository
    }

    private lateinit var origenHost: String
    private lateinit var origenIp: String
    private lateinit var destinoHost: String
    private lateinit var destinoIp: String
    private lateinit var protocolo: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelnetConsoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            origenHost = it.getString("origenHost") ?: ""
            origenIp = it.getString("origenIp") ?: ""
            destinoHost = it.getString("destinoHost") ?: ""
            destinoIp = it.getString("destinoIp") ?: ""
            protocolo = it.getString("protocolo") ?: "OSPF"
        }

        binding.tvOrigen.text = "$origenHost\n$origenIp"
        binding.tvDestino.text = "$destinoHost\n$destinoIp"

        binding.btnTelnet.setOnClickListener {
            binding.tvOutput.text = getString(R.string.exec_telnet)
            ejecutarTelnet()
        }

        binding.btnCerrar.setOnClickListener { cerrarFragmentoTelnet() }
    }

    private fun ejecutarTelnet() {
        val request = TelnetRequestDto(
            origen = NodoDto(origenHost, origenIp),
            vecino = NodoDto(destinoHost, destinoIp)
        )

        Log.d("TelnetRequest", "Request: $request | Protocolo: $protocolo")

        lifecycleScope.launch {
            try {
                val response = when (protocolo.uppercase()) {
                    "BGP" -> telnetRepo.verificarBgp(request)
                    else -> telnetRepo.verificarOspf(request)
                }

                val output = response.resultado.salida_telnet
                    .ifBlank { getString(R.string.sin_salida) }

                binding.tvOutput.text = output
                Log.d("TelnetOutput", output)

            } catch (e: Exception) {
                val msg = e.message ?: getString(R.string.desconocido)
                binding.tvOutput.text = getString(R.string.excepcion_telnet, msg)
                Log.e("TelnetException", "Fallo al hacer Telnet", e)
            }
        }
    }

    private fun cerrarFragmentoTelnet() {
        requireActivity().supportFragmentManager.beginTransaction()
            .remove(this)
            .commit()

        requireActivity()
            .findViewById<View>(R.id.telnet_container)
            .visibility = View.GONE
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
