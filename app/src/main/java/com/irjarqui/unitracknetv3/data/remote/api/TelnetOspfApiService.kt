package com.irjarqui.unitracknetv3.data.remote.api

import com.irjarqui.unitracknetv3.data.remote.model.telnet.TelnetRequestDto
import com.irjarqui.unitracknetv3.data.remote.model.telnet.TelnetResponseDto
import com.irjarqui.unitracknetv3.data.remote.model.telnet.TelnetWrapperDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TelnetOspfApiService {
    @POST("ospf/verificar")
    suspend fun verificarOspfTelnet(
        @Body request: TelnetRequestDto
    ): TelnetWrapperDto
}

