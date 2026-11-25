package com.citylisten.domain.repository

import com.citylisten.domain.model.Address
import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.result.DomainResult

interface AddressRepository {
    suspend fun reverseGeocode(point: GeoPoint, language: String): DomainResult<Address>
}
