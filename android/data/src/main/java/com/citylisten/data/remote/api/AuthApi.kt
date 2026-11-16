package com.citylisten.data.remote.api

import com.citylisten.data.remote.dto.AuthResponseDto
import retrofit2.http.POST

interface AuthApi {
    @POST("v1/auth/anonymous")
    suspend fun getAnonymousToken(): AuthResponseDto
}