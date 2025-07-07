package com.irjarqui.unitracknetv3.data.remote.api

import com.irjarqui.unitracknetv3.data.remote.model.profile.UserInfoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserInfoApi {
    @GET("usuario/{username}")
    suspend fun getUserInfo(@Path("username") username: String): Response<UserInfoResponse>
}