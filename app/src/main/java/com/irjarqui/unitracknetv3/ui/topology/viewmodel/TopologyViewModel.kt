package com.irjarqui.unitracknetv3.ui.topology.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.irjarqui.unitracknetv3.data.remote.model.bgp.SegmentoBgpDto
import com.irjarqui.unitracknetv3.data.remote.model.ospf.SegmentoOspfDto
import com.irjarqui.unitracknetv3.data.remote.repository.BgpRepository
import com.irjarqui.unitracknetv3.data.remote.repository.OspfRepository
import com.irjarqui.unitracknetv3.ui.topology.model.SegmentUiModel
import com.irjarqui.unitracknetv3.utils.TopologyHelper
import com.irjarqui.unitracknetv3.utils.RetrofitHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope


class TopologyViewModel(application: Application) : AndroidViewModel(application) {

    private val ospfRepo = OspfRepository(RetrofitHelper.getOspfService())
    private val bgpRepo = BgpRepository(RetrofitHelper.getBgpService())

    private val _segmentosOspf = MutableLiveData<List<SegmentoOspfDto>>()
    val segmentosOspf: LiveData<List<SegmentoOspfDto>> = _segmentosOspf

    private val _segmentosBgp = MutableLiveData<List<SegmentoBgpDto>>()
    val segmentosBgp: LiveData<List<SegmentoBgpDto>> = _segmentosBgp

    private var ospfData: List<SegmentoOspfDto> = emptyList()
    private var bgpData: List<SegmentoBgpDto> = emptyList()

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> get() = _isRefreshing

    val segmentosCombinados = MediatorLiveData<List<SegmentUiModel>>()

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> = _errorMsg

    private val _isInitialLoading = MutableLiveData(false)
    val isInitialLoading: LiveData<Boolean> get() = _isInitialLoading


    init {
        segmentosCombinados.addSource(segmentosOspf) { ospf ->
            ospfData = ospf
            combinarYPublicar()
        }

        segmentosCombinados.addSource(segmentosBgp) { bgp ->
            bgpData = bgp
            combinarYPublicar()
        }
    }

    val timestampOspf = MutableLiveData<String?>()
    val timestampBgp = MutableLiveData<String?>()


    private fun combinarYPublicar() {
        val combinados = TopologyHelper.combinarSegmentos(ospfData, bgpData)
        segmentosCombinados.value = combinados
    }

    fun cargarSegmentos() = viewModelScope.launch {
        try {
            supervisorScope {

                val ospfJob = async(Dispatchers.IO) {
                    runCatching { ospfRepo.getRawOspfResponse() }
                }

                val bgpJob = async(Dispatchers.IO) {
                    runCatching { bgpRepo.getRawBgpResponse() }
                }

                ospfJob.await()
                    .onSuccess { resp ->
                        timestampOspf.value = resp?.timestamp
                        _segmentosOspf.value = resp?.segmentos ?: emptyList()
                    }
                    .onFailure { e ->
                        _segmentosOspf.value = emptyList()
                        timestampOspf.value = null
                        _errorMsg.value = "Error OSPF: ${e.localizedMessage}"
                    }

                bgpJob.await()
                    .onSuccess { resp ->
                        timestampBgp.value = resp?.timestamp
                        _segmentosBgp.value = resp?.segmentos ?: emptyList()
                    }
                    .onFailure { e ->
                        _segmentosBgp.value = emptyList()
                        timestampBgp.value = null
                        _errorMsg.value = "Error BGP: ${e.localizedMessage}"
                    }
            }
        } finally {
            _isRefreshing.value = false
            _isInitialLoading.value = false
        }

    }

    fun iniciarCarga() {
        ensureDataLoaded(forceRefresh = false)
    }

    fun ensureDataLoaded(forceRefresh: Boolean = false) {
        if (!forceRefresh &&
            !_segmentosOspf.value.isNullOrEmpty() &&
            !_segmentosBgp.value.isNullOrEmpty()
        ) return

        _isInitialLoading.value = true
        cargarSegmentos()
    }
}
