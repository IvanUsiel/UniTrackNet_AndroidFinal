package com.irjarqui.unitracknetv3.application

import android.app.Application
import android.util.Log
import com.irjarqui.unitracknetv3.data.remote.api.AuthApiService
import com.irjarqui.unitracknetv3.data.remote.api.BgpApiService
import com.irjarqui.unitracknetv3.data.remote.api.OspfApiService
import com.irjarqui.unitracknetv3.data.remote.repository.BgpRepository
import com.irjarqui.unitracknetv3.data.remote.repository.LoginRepository
import com.irjarqui.unitracknetv3.data.remote.repository.OspfRepository
import com.irjarqui.unitracknetv3.data.remote.repository.TelnetRepository
import com.irjarqui.unitracknetv3.ui.alarms.scheduleAlarmPolling
import com.irjarqui.unitracknetv3.utils.RetrofitHelper

class UniTrackNetApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("APP", "Lanzando scheduleAlarmPolling()")
        applicationContext.scheduleAlarmPolling()
    }

    val authApiService: AuthApiService by lazy {
        RetrofitHelper.getAuthService()
    }

    val ospfApiService: OspfApiService by lazy {
        RetrofitHelper.getOspfService()
    }

    val telnetBgpService by lazy {
        RetrofitHelper.getTelnetBgpService()
    }

    val telnetOspfService by lazy {
        RetrofitHelper.getTelnetOspfService()
    }

    val loginRepository: LoginRepository by lazy {
        LoginRepository(authApiService)
    }

    val ospfRepository: OspfRepository by lazy {
        OspfRepository(ospfApiService)
    }

    val telnetRepository: TelnetRepository by lazy {
        TelnetRepository(telnetBgpService, telnetOspfService)
    }

    val bgpApiService: BgpApiService by lazy {
        RetrofitHelper.getBgpService()
    }

    val bgpRepository: BgpRepository by lazy {
        BgpRepository(bgpApiService)
    }


}
