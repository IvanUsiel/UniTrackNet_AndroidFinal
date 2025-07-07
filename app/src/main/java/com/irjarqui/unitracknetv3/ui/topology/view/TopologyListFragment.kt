package com.irjarqui.unitracknetv3.ui.topology.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.irjarqui.unitracknetv3.databinding.FragmentOspfSegmentListBinding
import com.irjarqui.unitracknetv3.ui.topology.viewmodel.TopologyViewModel
import com.irjarqui.unitracknetv3.ui.topology.adapter.TopologyAdapter
import com.irjarqui.unitracknetv3.utils.TopologyHelper

class TopologyListFragment : Fragment() {

    private var _binding: FragmentOspfSegmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TopologyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOspfSegmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[TopologyViewModel::class.java]

        val adapter = TopologyAdapter(
            onClick = { },
            showOspf = true,
            showBgp = false
        )

        binding.rvSegmentos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSegmentos.adapter = adapter

        viewModel.segmentosOspf.observe(viewLifecycleOwner) { ospfList ->
            val bgpList = viewModel.segmentosBgp.value ?: emptyList()

            val combinados = TopologyHelper.combinarSegmentos(ospfList, bgpList)
            adapter.submitList(combinados)
        }


        viewModel.cargarSegmentos()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}