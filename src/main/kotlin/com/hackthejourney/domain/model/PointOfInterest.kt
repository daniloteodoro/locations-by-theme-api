package com.hackthejourney.domain.model

import com.hackthejourney.domain.service.Coordinate

enum class PointOfInterestType {
    UNKNOWN,
    BEACH,
    PARK,
    SIGHTS,
    HISTORICAL,
    NIGHTLIFE,
    RESTAURANT,
    SHOPPING,
    CAMPING,
    CLIMBING,
    CYCLING,
    GLIDING,
    DIVING,
    SKIING
}

data class PointOfInterest(
        val type: PointOfInterestType,
        val name: String,
        val url: String,
        val pictureUrl: String,
        val rating: Int?,
        val location: Coordinate
)
