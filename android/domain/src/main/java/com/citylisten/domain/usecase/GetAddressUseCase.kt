package com.citylisten.domain.usecase

import com.citylisten.domain.model.Address
import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.repository.AddressRepository
import com.citylisten.domain.result.DomainResult
import javax.inject.Inject

class GetAddressUseCase @Inject constructor(
    private val addressRepository: AddressRepository
) {
    suspend operator fun invoke(point: GeoPoint, language: String): DomainResult<Address> =
        addressRepository.reverseGeocode(point, language)
}
