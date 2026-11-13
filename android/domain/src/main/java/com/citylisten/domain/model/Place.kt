package com.citylisten.domain.model

data class Place(
    val id: String,
    val name: String?,
    val type: PlaceType,
    val location: GeoPoint,
    val facts: List<Fact> = emptyList(),
    val source: DataSource = DataSource.OSM
)

enum class PlaceType {
    MONUMENT, BUILDING, PARK, SHOP, CAFE, STREET, UNKNOWN
}

data class Fact(
    val text: String,
    val source: FactSource
)

enum class FactSource { OSM, WIKIDATA, WIKIPEDIA }

enum class DataSource { OSM, WIKIDATA, WIKIPEDIA }