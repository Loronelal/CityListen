package com.citylisten.data.remote.api

import com.citylisten.data.remote.dto.AuthResponseDto
import com.citylisten.data.remote.dto.GenerateStoryRequest
import com.citylisten.data.remote.dto.StoryDto
import retrofit2.http.Body
import retrofit2.http.POST

interface CityListenApi {
    @POST("v1/auth/anonymous")
    suspend fun getAnonymousToken(): AuthResponseDto

    @POST("v1/stories")
    suspend fun generateStory(@Body request: GenerateStoryRequest): StoryDto
}