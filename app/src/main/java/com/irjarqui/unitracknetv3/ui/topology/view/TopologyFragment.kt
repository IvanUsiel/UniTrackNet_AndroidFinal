package com.irjarqui.unitracknetv3.ui.topology.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.databinding.FragmentTopologyBinding
import com.irjarqui.unitracknetv3.ui.topology.adapter.TopologyAdapter
import com.irjarqui.unitracknetv3.ui.topology.model.SegmentUiModel
import com.irjarqui.unitracknetv3.ui.topology.viewmodel.TopologyViewModel
import com.irjarqui.unitracknetv3.ui.topology.viewmodel.TopologyViewModelFactory
import com.irjarqui.unitracknetv3.utils.DateFormatUtils
import android.content.res.ColorStateList
import androidx.fragment.app.activityViewModels
import com.irjarqui.unitracknetv3.data.remote.repository.UserInfoRepository
import com.irjarqui.unitracknetv3.ui.profile.viewmodel.UserViewModel
import com.irjarqui.unitracknetv3.ui.profile.viewmodel.UserViewModelFactory
import com.irjarqui.unitracknetv3.utils.RetrofitHelper
import android.content.Context
import android.net.ConnectivityManager

class TopologyFragment : Fragment() {

    private var _binding: FragmentTopologyBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TopologyAdapter
    private lateinit var mapFragment: TopologyMapFragment
    private var showOspf = true
    private var showBgp = true
    private var segmentoSeleccionado: SegmentUiModel? = null


    private val viewModel: TopologyViewModel by activityViewModels {
        TopologyViewModelFactory(requireActivity().application)
    }

    private val userVM: UserViewModel by activityViewModels {
        UserViewModelFactory(UserInfoRepository(RetrofitHelper.getUserInfoService()))
    }





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopologyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userVM.userInfo.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvWelcome.text = getString(R.string.bienvenido___)
                binding.tvUserName.text = it.fullname
                binding.tvArea.text = getString(R.string.rol___, it.role)
            }
        }

        val isOnline = isNetworkAvailable(requireContext())
        binding.tvStatus.text = if (isOnline) getString(R.string.online) else getString(R.string.offline)
        binding.status.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isOnline) R.color.green_theme else R.color.red
            )
        )

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.ensureDataLoaded(forceRefresh = true)
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { refreshing ->
            binding.swipeRefresh.isRefreshing = refreshing
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { refreshing ->
            binding.swipeRefresh.isRefreshing = refreshing
        }

        viewModel.isInitialLoading.observe(viewLifecycleOwner) { loading ->
            binding.loadingOverlay.root.visibility = if (loading) View.VISIBLE else View.GONE
        }

        adapter = TopologyAdapter(
            onClick = { segmento ->
                segmentoSeleccionado = segmento
                val verifOspf = if (showOspf) segmento.verificacionesOspf else emptyList()
                val verifBgp = if (showBgp) segmento.verificacionesBgp else emptyList()

                mapFragment.clearMap()
                mapFragment.drawSegmentosCombinados(verifOspf, verifBgp)
            },
            showOspf = showOspf,
            showBgp = showBgp
        )
        binding.rvNodes.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvNodes.adapter = adapter

        if (childFragmentManager.findFragmentById(R.id.fragment_container) is TopologyMapFragment) {
            mapFragment = childFragmentManager.findFragmentById(R.id.fragment_container) as TopologyMapFragment
        } else {
            mapFragment = TopologyMapFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .commitNow()
        }

        showOspf = true
        showBgp = true
        binding.chipOspf.isChecked = true
        binding.chipBgp.isChecked = true
        actualizarEstiloChips()

        binding.chipOspf.setOnCheckedChangeListener { _, isChecked ->
            showOspf = isChecked
            actualizarEstiloChips()
            updateUi()
        }
        binding.chipBgp.setOnCheckedChangeListener { _, isChecked ->
            showBgp = isChecked
            actualizarEstiloChips()
            updateUi()
        }

        viewModel.segmentosCombinados.observe(viewLifecycleOwner) { updateUi() }
        viewModel.timestampOspf.observe(viewLifecycleOwner) { raw ->
            val fechaOspf = DateFormatUtils.formatFechaHoyODiferencia(raw)
            if (fechaOspf?.startsWith(getString(R.string.desactualizado)) == true) {
                val dias = fechaOspf.split(":").getOrNull(1) ?: "?"
                showSnackbarError(getString(R.string.ospf_estatus_desactualizado_por_d_as, dias))
                binding.tvTimestampOspf.text = getString(R.string.ospf, raw)
                binding.tvTimestampOspf.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            } else {
                binding.tvTimestampOspf.text = getString(R.string.ospffecha, fechaOspf)
                binding.tvTimestampOspf.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_theme))
            }
        }
        viewModel.timestampBgp.observe(viewLifecycleOwner) { raw ->
            val fechaBgp = DateFormatUtils.formatFechaHoyODiferencia(raw)
            if (fechaBgp?.startsWith(getString(R.string.desactualizado)) == true) {
                val dias = fechaBgp.split(":").getOrNull(1) ?: "?"
                showSnackbarError(getString(R.string.bgp_estatus_desactualizado_por_d_as, dias))
                binding.tvTimestampBgp.text = getString(R.string.bgp, raw)
                binding.tvTimestampBgp.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            } else {
                binding.tvTimestampBgp.text = getString(R.string.bgp_fecha, fechaBgp)
                binding.tvTimestampBgp.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_theme))
            }
        }
        viewModel.ensureDataLoaded()

    }


    private fun updateUi() {
        if (!showOspf && !showBgp) {
            val combinados = viewModel.segmentosCombinados.value.orEmpty()
            val vacios = combinados.map { it.copy(verificacionesOspf = emptyList(), verificacionesBgp = emptyList()) }
            adapter.showOspf = false
            adapter.showBgp = false
            adapter.submitList(vacios)
            segmentoSeleccionado = null
            mapFragment.clearMap()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.selecciona_un_protocolo))
                .setMessage(getString(R.string.por_favor_selecciona_al_menos_ospf_o_bgp_para_visualizar_los_enlaces))
                .setPositiveButton(getString(R.string.entendido___)) { dialog, _ -> dialog.dismiss() }
                .show()
            return
        }

        val combinados = viewModel.segmentosCombinados.value.orEmpty()
        val filtrados = combinados.mapNotNull { segmento ->
            val verifOspf = if (showOspf) segmento.verificacionesOspf else emptyList()
            val verifBgp = if (showBgp) segmento.verificacionesBgp else emptyList()
            if (verifOspf.isNotEmpty() || verifBgp.isNotEmpty()) {
                segmento.copy(verificacionesOspf = verifOspf, verificacionesBgp = verifBgp)
            } else null
        }

        adapter.showOspf = showOspf
        adapter.showBgp = showBgp
        adapter.submitList(filtrados)

        if (segmentoSeleccionado != null) {
            val seg = filtrados.find { it.nombre == segmentoSeleccionado!!.nombre }
            if (seg != null) {
                mapFragment.clearMap()
                mapFragment.drawSegmentosCombinados(
                    if (showOspf) seg.verificacionesOspf else emptyList(),
                    if (showBgp) seg.verificacionesBgp else emptyList()
                )
            } else {
                segmentoSeleccionado = null
                mapFragment.clearMap()
            }
        } else {
            mapFragment.clearMap()
            mapFragment.drawSegmentosCombinadosGlobal(filtrados, showOspf, showBgp)
        }
    }

    private fun showSnackbarError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.red))
            .show()
    }

    private fun actualizarEstiloChips() {
        val colorOspf = if (showOspf)
            ContextCompat.getColor(requireContext(), R.color.green_theme)
        else
            ContextCompat.getColor(requireContext(), R.color.gray)

        val colorBgp = if (showBgp)
            ContextCompat.getColor(requireContext(), R.color.blue_theme)
        else
            ContextCompat.getColor(requireContext(), R.color.gray)

        binding.chipOspf.chipStrokeColor = ColorStateList.valueOf(colorOspf)
        binding.chipBgp.chipStrokeColor = ColorStateList.valueOf(colorBgp)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnectedOrConnecting == true
    }



    fun onMapaListo() {
        updateUi()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
