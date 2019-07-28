package com.hackthejourney.application.dto

import com.hackthejourney.domain.model.*
import java.time.LocalDate

data class ThemeSimple(
        val name: String,
        val description: String,
        val image: String
) {
    companion object {
        fun of(theme: Theme) =
                ThemeSimple(theme.name, theme.description, theme.image)
    }
}

data class TravelSuggestion (
        val city: String,
        val country: String,
        val picture: String, // URL
        val cheapestPrice: Double,
        val locationUrl: String,
        val departureDate: LocalDate,
        val returnDate: LocalDate,
        val maxTemperature: Int,
        val minTemperature: Int,
        val maxWind: Int?,
        val precipitation: Double?,
        val weatherStatus: String,
        val holidays: List<Holiday>,
        val poiList: List<PointOfInterest>,
        val flights: List<FlightOffer>
)

enum class FlightTipType {
    HINT,
    WARNING,
    CRITICAL
}

data class FlightTip(
        val type: FlightTipType,
        val message: String
)

data class PlannedTravel (
        val origin: City,
        val destiny: City,
        val flights: List<FlightOffer>,
        val tips: List<FlightTip>,
        val links: List<String>
)

data class TravelApiResponse<T> (val data: T)
