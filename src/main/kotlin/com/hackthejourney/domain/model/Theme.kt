package com.hackthejourney.domain.model

import java.util.*

data class Theme(
        val name: String,
        val description: String,
        val image: String,
        val filter: TravelFilter) {

    fun matches(travelLocation: TravelLocation): FilterResult {

        /*
            Example: Theme 'Going to the beach'
                Minimum temperature: 20º C
                Maximum temperature: NULL
                Maximum Wind speed: 40 km/h
                Weather Status: Clear day
                POI: Should have "Beach/Park"
         */

        // Check whether wind speed should be enforced
        if (filter.maxWindSpeed.isPresent) {
            if (travelLocation.weather.isEmpty())
                return FilterResult.fail("Location $travelLocation does not contain required attribute 'Weather'.")

            val windSpeedFilter = filter.maxWindSpeed.get()
            if (travelLocation.weather.maxWind == null)
                return FilterResult.fail("Location $travelLocation does not contain required attribute 'Maximum wind speed'.")
            else if (travelLocation.weather.maxWind!! > windSpeedFilter)
                return FilterResult.fail("Maximum wind speed ${travelLocation.weather.maxWind} for this location ($travelLocation) is greater than: $windSpeedFilter.")
        }

        if (filter.maxTemperature.isPresent) {
            if (travelLocation.weather.isEmpty())
                return FilterResult.fail("Location $travelLocation does not contain required attribute 'Weather'.")

            val temperatureFilter = filter.maxTemperature.get()
            if (travelLocation.weather.maxTemperature > temperatureFilter)
                return FilterResult.fail("Maximum temperature ${travelLocation.weather.maxTemperature} in $travelLocation is greater than: $temperatureFilter.")
        }

        if (filter.minTemperature.isPresent) {
            if (travelLocation.weather.isEmpty())
                return FilterResult.fail("Location $travelLocation does not contain required attribute 'Weather'.")

            val temperatureFilter = filter.minTemperature.get()
            if (travelLocation.weather.minTemperature < temperatureFilter)
                return FilterResult.fail("Minimum temperature ${travelLocation.weather.minTemperature} in $travelLocation is less than: $temperatureFilter.")
        }

        if (filter.weatherStatus.isNotEmpty()) {
            if (travelLocation.weather.isEmpty())
                return FilterResult.fail("Location $travelLocation does not contain required attribute 'Weather'.")

            // Weather status can be one of the possible values, e.g. filter can accept Clear sky or cloudy
            if (travelLocation.weather.status !in filter.weatherStatus)
                return FilterResult.fail("Weather status at the Location $travelLocation (${travelLocation.weather.status}) is incompatible with allowed values: ${filter.weatherStatus.joinToString(",")} .")
        }

        if (filter.POI.isNotEmpty()) {
            // Travel location must have all specified POIs
            if (!travelLocation.getPOITypes().containsAll(filter.POI))
                return FilterResult.fail("POI in the location $travelLocation (${travelLocation.POI.joinToString(",")}) is incompatible with allowed values: ${filter.POI.joinToString(",")} .")
        }

        return FilterResult.passed()
    }

}

// TODO: Create builder, if necessary
data class TravelFilter(
        val maxTemperature: Optional<Int>,
        val minTemperature: Optional<Int>,
        val maxWindSpeed: Optional<Int>,
        val weatherStatus: Set<WeatherStatus>,
        val POI: Set<PointOfInterestType>
)

data class FilterResult private constructor(
        val passed: Boolean,
        val reasonToRemove: String = ""
) {
    companion object {
        fun passed() =
                FilterResult(true)
        fun fail(reason: String) =
                FilterResult(false, reason)
    }
}
