package com.irjarqui.unitracknetv3.data.remote.api

import com.irjarqui.unitracknetv3.data.remote.model.auth.AddIpRequestDto
import com.irjarqui.unitracknetv3.data.remote.model.auth.LoginRequestDto
import com.irjarqui.unitracknetv3.data.remote.model.auth.LoginResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    //Produccion uso de vpncooroporativa y credenciales
    //@POST("cgi-bin/auth_movil_tacacs.cgi")

    //Pruebas
    @POST("tacacs/autenticar")
    suspend fun login(@Body loginRequestDto: LoginRequestDto): Response<LoginResponseDto>

    @POST("cgi-bin/auth_movil_tacacs_addip.cgi")
    suspend fun addIp(@Body request: AddIpRequestDto): Response<LoginResponseDto>
}