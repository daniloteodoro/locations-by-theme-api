package com.hackthejourney.domain.service

import com.hackthejourney.domain.ReasonToTravelException
import com.hackthejourney.domain.model.WeatherStatus
import java.time.LocalDate
import java.time.LocalDateTime

interface WeatherDiscovery {

    fun byCoordinate(coordinate: Coordinate, date: LocalDate): WeatherInfo

}

data class WeatherInfo private constructor(
        val dateTime: LocalDateTime,
        val maxTemperature: Int,
        val minTemperature: Int,
        val precipitationProbability: Double?,
        val maxWind: Int?,
        val status: WeatherStatus
) {

    fun isEmpty() =
            this == EMPTY

    companion object {
        val EMPTY = WeatherInfo(LocalDateTime.now(), -1, -1, -1.0, -1, WeatherStatus.OTHERS)

        fun of(dateTime: LocalDateTime, maxTemperature: Int, minTemperature: Int, precipitationProbability: Double?, maxWind: Int?, status: WeatherStatus): WeatherInfo {
            if (maxTemperature !in -50..+50)
                throw InvalidWeatherInfo("Maximum temperature is out of range: $maxTemperature")
            if (minTemperature !in -50..+50)
                throw InvalidWeatherInfo("Minimum temperature is out of range: $minTemperature")
            if (precipitationProbability != null && (precipitationProbability < 0 || precipitationProbability > 1))
                throw InvalidWeatherInfo("Precipitation probability is out of range: $precipitationProbability (should be between 0 and 1)")
            if (maxWind != null && maxWind < 0)
                throw InvalidWeatherInfo("Maximum wind cannot be negative: $maxWind")
            return WeatherInfo(dateTime, maxTemperature, minTemperature, precipitationProbability, maxWind, status)
        }
    }

}

data class Coordinate(
        // TODO: Create V.O.
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
)

class InvalidWeatherInfo(msg: String) : ReasonToTravelException(msg)
