package com.hackthejourney.infrastructure.service

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.hackthejourney.application.DependencyError
import com.hackthejourney.domain.model.PointOfInterest
import com.hackthejourney.domain.model.PointOfInterestType
import com.hackthejourney.domain.model.PointOfInterestType.*
import com.hackthejourney.domain.service.Coordinate
import com.hackthejourney.domain.service.PointOfInterestDiscovery
import com.hackthejourney.infrastructure.json.fromJson
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service


@Service
class PointOfInterestDiscoveryYelp(@Value("\${yelp.api.url}") private val yelpApiUrl: String,
                                   @Value("\${YELP_API_KEY}") private val yelpApiKey: String) : PointOfInterestDiscovery {

    private val typeToCategory = mapOf<PointOfInterestType, List<YelpCategory>>(
            BEACH to listOf(YelpCategory("beaches")),
            CAMPING to listOf(YelpCategory("campgrounds"),YelpCategory("summer_camps"),YelpCategory("daycamps")),
            CLIMBING to listOf(YelpCategory("climbing"),YelpCategory("rock_climbing")),
            CYCLING to listOf(YelpCategory("bicyclepaths"), YelpCategory("bikerentals")),
            GLIDING to listOf(YelpCategory("hanggliding"),YelpCategory("paragliding"),YelpCategory("skydiving"),YelpCategory("parasailing")),
            DIVING to listOf(YelpCategory("snorkeling"),YelpCategory("scuba"),YelpCategory("freediving"),YelpCategory("diving"),YelpCategory("diveshops")),
            SKIING to listOf(YelpCategory("skiing"),YelpCategory("skiresorts"),YelpCategory("skischools")),
            PARK to listOf(YelpCategory("parks"))
    )

    private fun getYelpCategoriesByType(types: List<PointOfInterestType>) =
            types.mapNotNull {
                typeToCategory[it]?.map {
                    cat -> cat.alias
                }?.joinToString(",")
            }.joinToString(",")

    private fun getTypeByYelpCategories(items: List<YelpCategory>): PointOfInterestType {
        for (current: Map.Entry<PointOfInterestType, List<YelpCategory>> in typeToCategory) {
            if (current.value.intersect(items).isNotEmpty())
                return current.key
        }
        return UNKNOWN
    }

    override fun byCoordinateAndType(coordinate: Coordinate, types: List<PointOfInterestType>): List<PointOfInterest> {
        val defaultRadiusInMeters = 40000

        // TODO: If service does not respond on time we can still continue, but showing an additional warning as: Cannot retrieve POI from Yelp

        // Note: at this time, the Yelp API does not return businesses without any reviews: https://www.yelp.com/developers/documentation/v3/business
        val params = listOf("latitude" to coordinate.latitude, "longitude" to coordinate.longitude, "radius" to defaultRadiusInMeters, "categories" to getYelpCategoriesByType(types))
        val (_, _, result) = yelpApiUrl.httpGet(params).header("Authorization" to "Bearer $yelpApiKey")

                // Make sure service responds on time
                .timeoutRead(2000)

                .responseString()

        val jsonResult = when (result) {
            is Result.Failure -> {
                throw DependencyError("Error getting Points of interest:" + result.getException().message)
            }
            is Result.Success -> {
                result.get()
            }
        }

        val records = jsonResult.fromJson<PlaceResults>(PropertyNamingStrategy.SNAKE_CASE).businesses

        return if (records.isNotEmpty()) {
            records.map {
                PointOfInterest(
                        getTypeByYelpCategories(it.categories.toList()),
                        it.name.take(30),
                        it.url,
                        it.imageUrl,
                        it.rating,
                        Coordinate(it.coordinates.latitude, it.coordinates.longitude)
                )
            }
        } else {
            emptyList()
        }

    }

}


data class PlaceResults (
        val businesses: Array<BusinessDetail> = arrayOf()
)

data class BusinessDetail(
        val id: String = "",
        val name: String = "",
        val coordinates: Coordinate = Coordinate(0.0, 0.0),
        val categories: Array<YelpCategory> = arrayOf(),
        val url: String = "",
        val imageUrl: String = "",
        val rating: Int? = null
)

data class YelpCategory(
        val alias: String = ""
)
