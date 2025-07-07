package com.irjarqui.unitracknetv3.ui.profile.viewmodel

import androidx.lifecycle.*
import com.irjarqui.unitracknetv3.data.remote.model.profile.UserInfoResponse
import com.irjarqui.unitracknetv3.data.remote.repository.UserInfoRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repo: UserInfoRepository) : ViewModel() {

    private val _userInfo = MutableLiveData<UserInfoResponse?>()
    val userInfo: LiveData<UserInfoResponse?> = _userInfo

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> get() = _isRefreshing

    fun refresh(username: String) {
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                val resp = repo.fetchUserInfo(username)
                if (resp.isSuccessful) {
                    _userInfo.value = resp.body()
                } else _error.value = "Usuario no encontrado"
            } catch (e: Exception) {
                _error.value = "Error al cargar datos"
            } finally {
                _isRefreshing.value = false
            }
        }
    }


}
