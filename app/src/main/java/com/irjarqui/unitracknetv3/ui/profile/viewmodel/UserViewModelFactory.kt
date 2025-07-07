package com.irjarqui.unitracknetv3.ui.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.irjarqui.unitracknetv3.data.remote.repository.UserInfoRepository

class UserViewModelFactory(
    private val userRepo: UserInfoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(userRepo) as T
    }
}
