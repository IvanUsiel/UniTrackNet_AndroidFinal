package com.irjarqui.unitracknetv3.data.remote.repository

import com.irjarqui.unitracknetv3.data.remote.api.AuthApiService
import com.irjarqui.unitracknetv3.data.remote.model.auth.AddIpRequestDto
import com.irjarqui.unitracknetv3.data.remote.model.auth.LoginRequestDto
import com.irjarqui.unitracknetv3.data.remote.model.auth.LoginResponseDto
import retrofit2.Response

class LoginRepository(private val apiService: AuthApiService) {
    suspend fun login(loginRequestDto: LoginRequestDto): Response<LoginResponseDto> {
        return apiService.login(loginRequestDto)
    }

    suspend fun addIp(request: AddIpRequestDto): Response<LoginResponseDto> {
        return apiService.addIp(request)
    }
}