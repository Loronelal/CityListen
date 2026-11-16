package com.citylisten.data.remote

import com.citylisten.domain.result.DomainError
import com.citylisten.domain.result.DomainErrorType
import com.citylisten.domain.result.DomainResult
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T> safeApiCall(block: suspend () -> T): DomainResult<T> {
    return try {
        DomainResult.Success(block())
    } catch (e: SocketTimeoutException) {
        DomainResult.Failure(DomainError(DomainErrorType.TIMEOUT, "Превышено время ожидания", e))
    } catch (e: IOException) {
        DomainResult.Failure(DomainError(DomainErrorType.NETWORK, "Нет подключения к интернету", e))
    } catch (e: HttpException) {
        val type = if (e.code() >= 500) DomainErrorType.SERVER else DomainErrorType.UNKNOWN
        DomainResult.Failure(DomainError(type, "Ошибка сервера: ${e.code()}", e))
    } catch (e: Exception) {
        DomainResult.Failure(DomainError(DomainErrorType.UNKNOWN, "Неизвестная ошибка", e))
    }
}