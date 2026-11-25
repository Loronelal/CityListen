package com.citylisten.data.repository

import com.citylisten.data.remote.api.NominatimApi
import com.citylisten.data.remote.mapper.toDomain
import com.citylisten.data.remote.safeApiCall
import com.citylisten.domain.model.Address
import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.repository.AddressRepository
import com.citylisten.domain.result.DomainError
import com.citylisten.domain.result.DomainErrorType
import com.citylisten.domain.result.DomainResult
import javax.inject.Inject

class NominatimAddressRepository @Inject constructor(
    private val api: NominatimApi
) : AddressRepository {

    override suspend fun reverseGeocode(
        point: GeoPoint,
        language: String
    ): DomainResult<Address> {
        val result = safeApiCall {
            api.reverse(
                latitude = point.latitude,
                longitude = point.longitude,
                language = language.take(2)
            )
        }
        return when (result) {
            is DomainResult.Success -> {
                val address = result.data.toDomain()
                if (address == null) {
                    DomainResult.Failure(
                        DomainError(DomainErrorType.UNKNOWN, "Не удалось определить адрес")
                    )
                } else {
                    DomainResult.Success(address)
                }
            }
            is DomainResult.Failure -> result
        }
    }
}
