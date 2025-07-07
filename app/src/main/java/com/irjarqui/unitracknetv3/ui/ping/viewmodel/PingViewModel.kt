package com.irjarqui.unitracknetv3.ui.ping.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irjarqui.unitracknetv3.data.remote.model.ping.PingRequest
import com.irjarqui.unitracknetv3.data.remote.model.ping.PingResponse
import com.irjarqui.unitracknetv3.data.remote.repository.PingRepository
import kotlinx.coroutines.launch

class PingViewModel(
    private val repo: PingRepository
) : ViewModel() {

    private val _stats = MutableLiveData<PingResponse.Resultado>()
    val stats: LiveData<PingResponse.Resultado> = _stats

    val isLoading = MutableLiveData(false)

    fun runPing(origenIp: String, destinoIp: String) = viewModelScope.launch {
        isLoading.value = true

        val request = PingRequest(
            origen = PingRequest.Nodo("origen",  origenIp),
            destino = PingRequest.Nodo("destino", destinoIp)
        )

        val response = repo.doPing(request)
        val body = response.body()
        if (response.isSuccessful && body != null) {
            _stats.value = body.resultado
        }

        isLoading.value = false
    }
}
