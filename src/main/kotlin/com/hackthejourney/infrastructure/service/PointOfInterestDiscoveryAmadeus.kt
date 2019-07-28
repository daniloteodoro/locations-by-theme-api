package com.hackthejourney.infrastructure.service

import com.amadeus.Amadeus
import com.amadeus.Params
import com.amadeus.resources.Resource
import com.hackthejourney.domain.model.PointOfInterest
import com.hackthejourney.domain.model.PointOfInterestType
import com.hackthejourney.domain.service.Coordinate
import com.hackthejourney.domain.service.PointOfInterestDiscovery
import org.springframework.stereotype.Service
import com.amadeus.resources.PointOfInterest as AmadeusPointOfInterest


// TODO: Re-enable this service
//@Service
class PointOfInterestDiscoveryAmadeus(private val amadeus: Amadeus) : PointOfInterestDiscovery {

    private val amadeusCategoryToType = mapOf<String, List<PointOfInterestType>>(
            "BEACH_PARK" to listOf(PointOfInterestType.BEACH, PointOfInterestType.PARK),
            "SIGHTS" to listOf(PointOfInterestType.SIGHTS)
    )

    override fun byCoordinateAndType(coordinate: Coordinate, types: List<PointOfInterestType>): List<PointOfInterest> {
        val defaultRadiusInKm = 20

        // POIs are only available in a few cities on test environment: https://github.com/amadeus4dev/data-collection/blob/master/data/pois.md
        val pointsOfInterest = try {
            val POIs = mutableListOf<AmadeusPointOfInterest>()

            POIs.addAll(amadeus.referenceData.locations.pointsOfInterest.get(Params
                    .with("latitude", coordinate.latitude)
                    .and("longitude", coordinate.longitude)
                    .and("radius", defaultRadiusInKm)))

            POIs.filter {
                amadeusCategoryToType[it.category]?.intersect(types)?.isNotEmpty() ?: false
            }

        } catch (e: Exception) {
            emptyList<AmadeusPointOfInterest>()
        }

        return pointsOfInterest.map {
            PointOfInterest(
                    PointOfInterestType.valueOf(it.category),
                    it.name.take(30),
                    "",
                    "",
                    null,
                    Coordinate(it.geoCode.latitude, it.geoCode.longitude)
            )
        }
    }

}
