package com.irjarqui.unitracknetv3.ui.alarms.viewmodel

import androidx.lifecycle.*
import com.irjarqui.unitracknetv3.data.remote.repository.BgpRepository
import com.irjarqui.unitracknetv3.data.remote.repository.OspfRepository
import com.irjarqui.unitracknetv3.ui.alarms.model.AlarmStatus
import com.irjarqui.unitracknetv3.ui.alarms.model.AlarmUiModel
import com.irjarqui.unitracknetv3.utils.TopologyHelper.buildAlarmList
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AlarmsViewModel(
    private val bgpRepo: BgpRepository,
    private val ospfRepo: OspfRepository
) : ViewModel() {

    private val _allAlarms   = MutableLiveData<List<AlarmUiModel>>()
    private val _filterText  = MutableLiveData("")
    private val _isRefreshing = MutableLiveData(false)

    val isRefreshing: LiveData<Boolean> get() = _isRefreshing

    val filteredAlarms: LiveData<List<AlarmUiModel>> =
        MediatorLiveData<List<AlarmUiModel>>().apply {
            addSource(_allAlarms) { value = applyFilter(it, _filterText.value.orEmpty()) }
            addSource(_filterText) { value = applyFilter(_allAlarms.value.orEmpty(), it) }
        }

    val networkStatus = MutableLiveData<AlarmStatus>()
    val lastBgpCheck  = MutableLiveData<String>()
    val lastOspfCheck = MutableLiveData<String>()

    init { fetchData() }

    fun updateFilter(text: String) { _filterText.value = text }

    fun fetchData() {
        _isRefreshing.value = true

        viewModelScope.launch {
            try {
                val bgp  = bgpRepo.getSegmentos() ?: emptyList()
                val ospf = ospfRepo.getSegmentos() ?: emptyList()

                val alarms = buildAlarmList(bgp, ospf)
                _allAlarms.value = alarms

                networkStatus.value =
                    if (alarms.any { it.status == AlarmStatus.ERROR }) AlarmStatus.ERROR
                    else AlarmStatus.OK

            } catch (e: Exception) {
                e.printStackTrace()
                networkStatus.value = AlarmStatus.ERROR
                _allAlarms.value = emptyList()
            } finally {
                val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                lastBgpCheck.value  = fmt.format(Date())
                lastOspfCheck.value = fmt.format(Date())
                _isRefreshing.value = false
            }
        }
    }

    fun setStatusFilter(status: AlarmStatus?) {
        statusFilter = status
        _filterText.value = _filterText.value
    }

    private var statusFilter: AlarmStatus? = null

    private fun applyFilter(list: List<AlarmUiModel>, filter: String): List<AlarmUiModel> =
        list.filter {
            (statusFilter == null || it.status == statusFilter) &&
                    (it.segmento.contains(filter, true) ||
                            it.ipA.contains(filter, true)      ||
                            it.ipB.contains(filter, true))
        }
}

class AlarmsViewModelFactory(
    private val bgpRepo: BgpRepository,
    private val ospfRepo: OspfRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AlarmsViewModel(bgpRepo, ospfRepo) as T
}
