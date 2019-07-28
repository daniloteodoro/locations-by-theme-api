package com.hackthejourney.infrastructure.service

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.hackthejourney.application.DependencyError
import com.hackthejourney.domain.model.WeatherStatus
import com.hackthejourney.domain.service.Coordinate
import com.hackthejourney.domain.service.WeatherDiscovery
import com.hackthejourney.domain.service.WeatherInfo
import com.hackthejourney.infrastructure.json.fromJson
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.*
import java.util.*
import kotlin.math.roundToInt

@Service
class WeatherDiscoveryDarkSky(@Value("\${darksky.url}") private val darkSkyUrl: String,
                              @Value("\${DARKSKY_API_KEY}") private val darkSkyApiKey: String) : WeatherDiscovery {

    override fun byCoordinate(coordinate: Coordinate, date: LocalDate): WeatherInfo {

        /*
            By default, DarkSky has day-by-day forecast of up to 7 days. Other providers can offer up to 30 days, but
            only with payed accounts. The tactics here will be using historical data to define weather when forecast is
            not available, thus helping the traveller to have an idea at least on the average temperature on that time.

            Another limitation of the free account is the amount of requests per day: 1000
            When searching using themes, one result can have multiple destinations, and the weather should be checked daily
            for each destination, yielding (destination_count * travel_duration_in_days) calls to the DarkSky API. To avoid
            reaching quickly to the limit, only weather for the first day will be checked. A commercial solution won't have
            this limitation.
         */
        val today = LocalDate.now()
        val forecastLimitDate = today.plusDays(7)

        return if (date in today..forecastLimitDate)
            getWeatherForecast(coordinate)
        else
            getHistoricalWeatherData(coordinate, date.minusYears(1))

    }

    private fun requestWeatherInfo(darkSkyUrl: String): WeatherInfo {

        val (_, _, result) = darkSkyUrl.httpGet(listOf("units" to "si", "exclude" to "currently,flags,minutely,hourly"))

                // TODO: If service does not respond on time we can still continue, but showing an additional warning as: Cannot retrieve weather
                .timeoutRead(2000)

                .responseString()

        val jsonResult = when (result) {
            is Result.Failure -> {
                throw DependencyError(result.getException().message ?: result.getException().javaClass.simpleName)
            }
            is Result.Success -> {
                result.get()
            }
        }

        if (jsonResult.isBlank())
            throw DependencyError("No information available for weather")

        // In some occasions there was no historical data for locations
        val weatherRecords = jsonResult.fromJson<WeatherResults>().daily.data

        return if (weatherRecords.isNotEmpty()) {
            weatherRecords.first().let {
                WeatherInfo.of(LocalDateTime.ofEpochSecond(it.time, 0, ZoneOffset.UTC),
                        it.temperatureMax.roundToInt(),
                        it.temperatureMin.roundToInt(),
                        it.precipProbability,
                        it.windSpeed?.roundToInt(),
                        it.getStatus())
            }
        } else {
            WeatherInfo.EMPTY
        }
    }

    private fun getWeatherForecast(coordinate: Coordinate): WeatherInfo {
        return requestWeatherInfo("$darkSkyUrl/$darkSkyApiKey/${coordinate.latitude},${coordinate.longitude}")
    }

    private fun getHistoricalWeatherData(coordinate: Coordinate, dateInThePast: LocalDate): WeatherInfo {

        if (dateInThePast.isAfter(LocalDate.now()))
            throw InvalidDateForHistoricalWeather("Date should be in the past: $dateInThePast")

        // TODO: Automatically add warning: using past data

        return requestWeatherInfo("$darkSkyUrl/$darkSkyApiKey/${coordinate.latitude},${coordinate.longitude},${dateInThePast.toEpochSecond(LocalTime.NOON, ZoneOffset.UTC)}")
    }

}

class InvalidDateForHistoricalWeather(msg: String) : Exception(msg)

data class WeatherResults(
        val daily: DailyWeatherData = DailyWeatherData()
)

data class DailyWeatherData(
        val data: Array<WeatherData> = arrayOf()
)

data class WeatherData(
        val time: Long = 0L,
        val icon: String = "",
        val summary: String = "",
        val precipProbability: Double? = null,
        val temperatureMin: Double = 0.0,
        val temperatureMax: Double = 0.0,
        val cloudCover: Double? = null,
        val windSpeed: Double? = null,
        val windGust: Double? = null
) {
    fun getStatus(): WeatherStatus {
        return when (icon) {
            "clear-day" -> WeatherStatus.CLEAR_DAY
            "clear-night" -> WeatherStatus.CLEAR_DAY
            "rain" -> WeatherStatus.RAINY
            "snow" -> WeatherStatus.SNOWY
            "sleet" -> WeatherStatus.RAINY
            "wind" -> WeatherStatus.OTHERS
            "fog" -> WeatherStatus.OTHERS
            "cloudy" -> WeatherStatus.CLEAR_DAY
            "partly-cloudy-day" -> WeatherStatus.CLEAR_DAY
            "partly-cloudy-night" -> WeatherStatus.CLEAR_DAY
            else -> WeatherStatus.OTHERS
        }
    }
}
