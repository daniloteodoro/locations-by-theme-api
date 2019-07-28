package com.hackthejourney.domain.model

import com.hackthejourney.domain.service.Coordinate
import com.hackthejourney.domain.service.WeatherInfo
import java.time.LocalDate

data class FlightOffer(
        val inboundAirline: String,
        val departureTime: String,
        val inboundDuration: String,
        val outboundAirline: String,
        val returnTime: String,
        val outboundDuration: String,
        val price: Double
)

data class TravelLocation(
        val city: City,
        val origin: String,
        val destination: String,
        val coordinates: Coordinate,
        val departure: LocalDate,
        val returnDate: LocalDate,
        val cheapestPrice: Double
) {

    var weather: WeatherInfo = WeatherInfo.EMPTY
    val holidays: MutableList<Holiday> = mutableListOf()
    val POI: MutableList<PointOfInterest> = mutableListOf()
    val flights: MutableList<FlightOffer> = mutableListOf()

    fun getPOITypes() =
            POI.map {
                it.type
            }

    fun getLocationUrl() =
            "https://www.google.com/maps/@${coordinates.latitude},${coordinates.longitude},13z"

    fun getPictureUrl() =
            if (POI.isNotEmpty())
                POI.first().pictureUrl
            else
                ""

    override fun toString(): String {
        return city.toString()
    }
}
