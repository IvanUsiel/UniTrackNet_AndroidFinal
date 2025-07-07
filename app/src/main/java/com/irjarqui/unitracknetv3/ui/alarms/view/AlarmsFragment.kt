package com.irjarqui.unitracknetv3.ui.alarms.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.application.UniTrackNetApp
import com.irjarqui.unitracknetv3.databinding.FragmentAlarmsBinding
import com.irjarqui.unitracknetv3.ui.alarms.adapter.AlarmAdapter
import com.irjarqui.unitracknetv3.ui.alarms.adapter.AlarmsSwipeCallback
import com.irjarqui.unitracknetv3.ui.alarms.model.AlarmStatus
import com.irjarqui.unitracknetv3.ui.alarms.model.AlarmUiModel
import com.irjarqui.unitracknetv3.ui.alarms.viewmodel.AlarmsViewModel
import com.irjarqui.unitracknetv3.ui.alarms.viewmodel.AlarmsViewModelFactory
import com.irjarqui.unitracknetv3.ui.ping.view.PingResultFragment
import com.irjarqui.unitracknetv3.ui.topology.view.TopologyStatusActivity

class AlarmsFragment : Fragment() {

    private lateinit var binding: FragmentAlarmsBinding
    private lateinit var adapter: AlarmAdapter
    private lateinit var viewModel: AlarmsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = requireActivity().application as UniTrackNetApp
        val factory = AlarmsViewModelFactory(app.bgpRepository, app.ospfRepository)

        viewModel = ViewModelProvider(requireActivity(), factory)[AlarmsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = AlarmAdapter { /* onItemClick */ }
        binding.rvAlarms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlarms.adapter = adapter

        ItemTouchHelper(
            AlarmsSwipeCallback(
                onTelnet = { alarm, pos ->
                    abrirTelnetFragment(alarm)
                    adapter.notifyItemChanged(pos)
                },
                onPing = { alarm, pos ->
                    abrirPingFragment(alarm)
                    adapter.notifyItemChanged(pos)
                }
            )
        ).attachToRecyclerView(binding.rvAlarms)

        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.updateFilter(text.toString())
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchData()
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { refreshing ->
            binding.swipeRefreshLayout.isRefreshing = refreshing
        }

        viewModel.filteredAlarms.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.networkStatus.observe(viewLifecycleOwner) { status ->
            val isOk = status == AlarmStatus.OK
            binding.headerAlarms.tvNetworkStatus.text =
                if (isOk) getString(R.string.network_status_stable) else getString(R.string.network_status_issues_detected)
            binding.headerAlarms.tvNetworkIcon.text =
                if (isOk) getString(R.string.check) else getString(
                    R.string.fail
                )

            val strokeColor = ContextCompat.getColor(
                requireContext(),
                if (isOk) R.color.green_theme else R.color.red
            )
            binding.headerAlarms.headerContainer.setStrokeColor(strokeColor)
        }

        viewModel.lastBgpCheck.observe(viewLifecycleOwner) {
            binding.headerAlarms.tvLastBgp.text = getString(R.string.last_bgp_check, it)
        }
        viewModel.lastOspfCheck.observe(viewLifecycleOwner) {
            binding.headerAlarms.tvLastOspf.text = getString(R.string.last_ospf_check, it)
        }

        binding.chipGroupStatus.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipAll -> viewModel.setStatusFilter(null)
                R.id.chipOk -> viewModel.setStatusFilter(AlarmStatus.OK)
                R.id.chipError -> viewModel.setStatusFilter(AlarmStatus.ERROR)
            }
        }
    }

    private fun abrirTelnetFragment(alarm: AlarmUiModel) {
        val bundle = Bundle().apply {
            putString(getString(R.string.origenhost), alarm.hostA)
            putString(getString(R.string.origenip), alarm.ipA)
            putString(getString(R.string.destinohost), alarm.hostB)
            putString(getString(R.string.destinoip), alarm.ipB)
            putString(getString(R.string.protocoloalarm), alarm.protocolo)
        }
        (requireActivity() as? TopologyStatusActivity)?.showTelnetOverlay(bundle)
    }

    private fun abrirPingFragment(alarm: AlarmUiModel) {
        PingResultFragment.new(
            ipA = alarm.ipA,
            ipB = alarm.ipB,
            hostA = alarm.hostA,
            hostB = alarm.hostB,
            protocolo = alarm.protocolo,
            segmento = alarm.segmento
        ).show(parentFragmentManager, getString(R.string.ping_))
    }
}
