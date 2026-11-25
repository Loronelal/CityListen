package com.citylisten.presentation.testing

import com.citylisten.domain.model.Address
import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.repository.AddressRepository
import com.citylisten.domain.result.DomainResult

class FakeAddressRepository(
    var result: DomainResult<Address> = DomainResult.Success(ADDRESS)
) : AddressRepository {

    val requests = mutableListOf<GeoPoint>()
    val languages = mutableListOf<String>()

    override suspend fun reverseGeocode(
        point: GeoPoint,
        language: String
    ): DomainResult<Address> {
        requests += point
        languages += language
        return result
    }

    companion object {
        val ADDRESS = Address(
            displayName = "Тверская улица, 1, Москва, Россия",
            road = "Тверская улица",
            houseNumber = "1",
            city = "Москва"
        )
    }
}
