package com.irjarqui.unitracknetv3.ui.ping.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.irjarqui.unitracknetv3.data.remote.repository.PingRepository

class PingVMFactory(
    private val repo: PingRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PingViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}