package com.citylisten.domain.model

data class Address(
    val displayName: String,
    val road: String? = null,
    val houseNumber: String? = null,
    val city: String? = null
) {
    val shortName: String
        get() {
            val street = listOfNotNull(road, houseNumber)
                .joinToString(", ")
                .takeIf { it.isNotBlank() }
            return listOfNotNull(street, city)
                .joinToString(", ")
                .ifBlank { displayName }
        }
}
