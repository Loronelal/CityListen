package com.citylisten.data.remote.auth

import com.citylisten.data.remote.api.AuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenProvider @Inject constructor(
    private val authApi: AuthApi
) {
    private val _token = MutableStateFlow<String?>(null)
    val token = _token.asStateFlow()
    private val mutex = Mutex()

    suspend fun getToken(): String? {
        _token.value?.let { return it }
        return mutex.withLock {
            _token.value?.let { return it }
            try {
                val response = authApi.getAnonymousToken()
                _token.value = response.token
                response.token
            } catch (e: Exception) {
                null
            }
        }
    }


    fun getTokenBlocking(): String? {
        return runBlocking {
            withTimeoutOrNull(5000) {
                getToken()
            }
        }
    }
}