package com.irjarqui.unitracknetv3.data.remote.repository

import com.irjarqui.unitracknetv3.data.remote.api.UserInfoApi
import com.irjarqui.unitracknetv3.data.remote.model.profile.UserInfoResponse
import retrofit2.Response

class UserInfoRepository(private val api: UserInfoApi) {
    suspend fun fetchUserInfo(username: String): Response<UserInfoResponse> {
        return api.getUserInfo(username)
    }
}