package com.hackthejourney.application.controller

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.hackthejourney.application.InvalidTheme
import com.hackthejourney.application.dto.TravelApiResponse
import com.hackthejourney.application.dto.TravelSuggestion
import com.hackthejourney.domain.repository.ThemeRepository
import com.hackthejourney.domain.service.DestinySelection
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@CrossOrigin
@RestController
@RequestMapping("/travels/{origin}", produces = [MediaType.APPLICATION_JSON_VALUE])
@Api(value = "travels", tags = ["Travels"], description = "Create travel suggestions based on themes like Skiing or going to the beach.")
class TravelController(private val destinySelection: DestinySelection,
                       private val themeRepository: ThemeRepository) {

    @ApiOperation("Return travel suggestions based on origin, period and a theme. Origin represents an iata airport code, such as BOS or SFO.")
    @GetMapping
    fun searchTravel(@PathVariable origin: String,
                     @RequestParam theme: String,
                     @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") start: LocalDate,
                     @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") end: LocalDate): ResponseEntity<TravelApiResponse<List<TravelSuggestion>>> {

        val selectedTheme = themeRepository.byName(theme) ?: throw InvalidTheme("Theme '$theme' does not exist")

        val suggestions = destinySelection.basedOn(origin, start, end, selectedTheme).map {
            TravelSuggestion(it.city.name, it.city.country.name, it.getPictureUrl(), it.cheapestPrice, it.getLocationUrl(), it.departure, it.returnDate,
                    it.weather.maxTemperature, it.weather.minTemperature, it.weather.maxWind, it.weather.precipitationProbability, it.weather.status.toString(),
                    it.holidays, it.POI, it.flights)
        }

        return ResponseEntity.ok(
                TravelApiResponse( suggestions )
        )
    }

}

open class AmadeusLocation private constructor(
        var subType: String,
        var detailedName: String
) {

    companion object {
        @JsonCreator
        fun initiate(@JsonProperty("subType") subType: String, @JsonProperty("detailedName") detailedName: String) =
                AmadeusLocation(subType, detailedName)
    }
}

typealias CurrencyAcronym = String
typealias CurrencyDescription = String
typealias CityAcronym = String

data class FlightDestinationDictionary(
        val currencies: MutableMap<CurrencyAcronym, CurrencyDescription> = mutableMapOf(),
        val locations: MutableMap<CityAcronym, AmadeusLocation> = mutableMapOf()
)

data class Dictionaries(
        val dictionaries: FlightDestinationDictionary = FlightDestinationDictionary()
)

