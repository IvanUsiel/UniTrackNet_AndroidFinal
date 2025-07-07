package com.irjarqui.unitracknetv3.ui.ping.view

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.github.anastr.speedviewlib.PointerSpeedometer
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.data.remote.repository.PingRepository
import com.irjarqui.unitracknetv3.databinding.FragmentPingResultBinding
import com.irjarqui.unitracknetv3.ui.ping.viewmodel.PingVMFactory
import com.irjarqui.unitracknetv3.ui.ping.viewmodel.PingViewModel
import com.irjarqui.unitracknetv3.utils.RetrofitHelper

class PingResultFragment : DialogFragment(R.layout.fragment_ping_result) {

    companion object {
        private const val ARG_IP_A = "ipA"
        private const val ARG_IP_B = "ipB"
        private const val ARG_HOST_A = "hostA"
        private const val ARG_HOST_B = "hostB"
        private const val ARG_PROTO = "protocolo"
        private const val ARG_SEGMENTO = "segmento"

        fun new(
            ipA: String,
            ipB: String,
            hostA: String,
            hostB: String,
            protocolo: String,
            segmento: String? = null
        ) = PingResultFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_IP_A, ipA)
                putString(ARG_IP_B, ipB)
                putString(ARG_HOST_A, hostA)
                putString(ARG_HOST_B, hostB)
                putString(ARG_PROTO, protocolo)
                putString(ARG_SEGMENTO, segmento)
            }
        }
    }

    private val vm by viewModels<PingViewModel> {
        PingVMFactory(PingRepository(RetrofitHelper.getPingService()))
    }

    private lateinit var b: FragmentPingResultBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b = FragmentPingResultBinding.bind(view)

        (b.gauge as? PointerSpeedometer)?.apply {
            withTremble = false
            unit = "ms"
            maxSpeed = 200f
            speedTo(0f, 0)
            unitTextColor = Color.WHITE
            speedTextColor = Color.WHITE
        }

        val ipA = requireArguments().getString(ARG_IP_A)!!
        val ipB = requireArguments().getString(ARG_IP_B)!!
        val hostA = requireArguments().getString(ARG_HOST_A)!!
        val hostB = requireArguments().getString(ARG_HOST_B)!!
        val protocolo = requireArguments().getString(ARG_PROTO)!!
        val segmento =
            requireArguments().getString(ARG_SEGMENTO) ?: getString(R.string.sin_segmento)

        b.tvSegmento.text = getString(R.string.segmento_ping, segmento)
        b.tvHostA.text = getString(R.string.punta_a_ping, hostA, ipA)
        b.tvHostB.text = getString(R.string.punta_b_ping, hostB, ipB)
        b.tvProtocolo.text = getString(R.string.protocolo_ping, protocolo.uppercase())

        vm.runPing(ipA, ipB)

        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            b.progress.visibility = if (loading) View.VISIBLE else View.GONE
        }

        vm.stats.observe(viewLifecycleOwner) { res ->
            res ?: return@observe
            b.gauge.speedTo(res.estadisticas.rtt_avg_ms.toFloat())
            b.tvStats.text = getString(
                R.string.ping_stats,
                res.estadisticas.rtt_min_ms,
                res.estadisticas.rtt_avg_ms,
                res.estadisticas.rtt_max_ms,
                res.estadisticas.paquetes_perdidos,
                res.estadisticas.paquetes_enviados
            )
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
