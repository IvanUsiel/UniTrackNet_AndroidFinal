package com.irjarqui.unitracknetv3.ui.topology.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TopologyViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopologyViewModel::class.java)) {
            return TopologyViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}