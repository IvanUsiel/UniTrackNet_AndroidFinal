package com.irjarqui.unitracknetv3.utils

import com.irjarqui.unitracknetv3.data.remote.api.AuthApiService
import com.irjarqui.unitracknetv3.data.remote.api.BgpApiService
import com.irjarqui.unitracknetv3.data.remote.api.OspfApiService
import com.irjarqui.unitracknetv3.data.remote.api.PingApiService
import com.irjarqui.unitracknetv3.data.remote.api.TelnetBgpApiService
import com.irjarqui.unitracknetv3.data.remote.api.TelnetOspfApiService
import com.irjarqui.unitracknetv3.data.remote.api.UserInfoApi
import com.irjarqui.unitracknetv3.data.remote.repository.PingRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitHelper {

    // URL Produccion
    //private const val BASE_URL_AUTH = "http://200.33.150.46:2002/"

    //URL Pruebas
    private const val BASE_URL_AUTH = "https://private-08d36c-apiautenticaciontacacsise.apiary-mock.com/"

    //URL Pruebas
    private const val BASE_URL_OSPF = "https://private-87799c-apiospfeuamex.apiary-mock.com/"

    //URL Produccion
    //private const val BASE_URL_OSPF = "http://200.33.150.46:2002/cgi-bin/"

    // URL Pruebas
    private const val BASE_URL_BGP = "https://private-b0c920-bgpsegmentoseuamexicoapi.apiary-mock.com/"

    //URL Produccion
    //private const val BASE_URL_BGP = "http://200.33.150.46:2002/cgi-bin/"

    private const val BASE_URL_BGP_TELNET = "https://private-083bd1-apiverificacionbgppuntoapuntotelnet.apiary-mock.com/"
    private const val BASE_URL_OSPF_TELNET = "https://private-bbeecd-apiverificacionospfpuntoapuntotelnet.apiary-mock.com/"

    private const val BASE_URL_PROFILE = "https://private-a7530e-iseinformationsimulate.apiary-mock.com/"
    private const val BASE_URL_PING = "https://private-87db0f-apiverificaciondeconectividadping.apiary-mock.com/"

    fun getUserInfoService(): UserInfoApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_PROFILE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserInfoApi::class.java)
    }


    fun getBgpService(): BgpApiService {
        return getRetrofit(BASE_URL_BGP).create(BgpApiService::class.java)
    }


    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val errorInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .build()

        val response = chain.proceed(request)

        when (response.code) {

            404 -> throw IOException("Recurso no encontrado (404)")
            500 -> throw IOException("Error interno del servidor (500)")
        }

        response
    }

    private fun getHttpClient(): OkHttpClient {
        val retryInterceptor = RetryInterceptor(
            maxRetries = 2,
            delayMs = 1_000L
        )

        return OkHttpClient.Builder()
            .connectTimeout(50, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(errorInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private fun getRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getAuthService(): AuthApiService {
        return getRetrofit(BASE_URL_AUTH).create(AuthApiService::class.java)
    }

    fun getOspfService(): OspfApiService {
        return getRetrofit(BASE_URL_OSPF).create(OspfApiService::class.java)
    }

    fun getTelnetBgpService(): TelnetBgpApiService {
        return getRetrofit(BASE_URL_BGP_TELNET).create(TelnetBgpApiService::class.java)
    }

    fun getTelnetOspfService(): TelnetOspfApiService {
        return getRetrofit(BASE_URL_OSPF_TELNET).create(TelnetOspfApiService::class.java)
    }

    fun getPingService(): PingApiService {
        return getRetrofit(BASE_URL_PING).create(PingApiService::class.java)
    }

}
