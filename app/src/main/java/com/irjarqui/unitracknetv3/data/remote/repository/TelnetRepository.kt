package com.irjarqui.unitracknetv3.data.remote.repository

import com.irjarqui.unitracknetv3.data.remote.api.TelnetBgpApiService
import com.irjarqui.unitracknetv3.data.remote.api.TelnetOspfApiService
import com.irjarqui.unitracknetv3.data.remote.model.telnet.TelnetRequestDto
import com.irjarqui.unitracknetv3.data.remote.model.telnet.TelnetWrapperDto
import com.irjarqui.unitracknetv3.ui.telnet.model.TelnetResponseUi

class TelnetRepository(
    private val bgpService: TelnetBgpApiService,
    private val ospfService: TelnetOspfApiService
) {
    suspend fun verificarBgp(request: TelnetRequestDto): TelnetWrapperDto {
        return bgpService.verificarBgpTelnet(request)
    }

    suspend fun verificarOspf(request: TelnetRequestDto): TelnetWrapperDto {
        return ospfService.verificarOspfTelnet(request)
    }
}

