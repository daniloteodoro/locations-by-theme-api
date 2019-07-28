package com.hackthejourney.infrastructure.service

import com.amadeus.Amadeus
import com.amadeus.Params
import com.hackthejourney.domain.model.*
import com.hackthejourney.domain.service.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import com.amadeus.resources.FlightOffer as FlightOfferFromAmadeus

typealias ExecutionTimeInMs = Long

// TODO: Maybe this entire class could be wrapped inside a domain object
// TODO: Process diagram for this class
@Service
class DestinySelectionAmadeus(private val amadeus: Amadeus,
                              private val holidayDiscovery: HolidayDiscovery,
                              private val weatherDiscovery: WeatherDiscovery,
                              private val pointOfInterestDiscovery: List<PointOfInterestDiscovery>) : DestinySelection {

    private fun <T>runWithTimingOptional(action: () -> T?): Pair<T?, ExecutionTimeInMs> {
        var result: T? = null
        val executionTime = measureTimeMillis {
            result = action()
        }
        return Pair(result, executionTime)
    }

    private fun <T>runWithTiming(action: () -> T): Pair<T, ExecutionTimeInMs> {
        val (result, time) = runWithTimingOptional(action)
        return Pair(result!!, time)
    }

    private fun CoroutineScope.fetchWeatherInformation(travelLocation: TravelLocation) = this.launch {
        try {
            val (weatherInformation, timeToGetWeather) = runWithTiming {
                weatherDiscovery.byCoordinate(travelLocation.coordinates, travelLocation.departure)
            }
            travelLocation.weather = weatherInformation
            LOGGER.info("Time to get weather information for ${travelLocation.city.name}: $timeToGetWeather ms")
        } catch (e: Exception) {
            LOGGER.severe("Fail to get weather information for ${travelLocation.city}: ${e.message}")
        }
    }

    private fun CoroutineScope.fetchAndUpdateHolidays(travelLocation: TravelLocation) = this.launch {
        try {
            val (holidays, timeToGetHolidays) = runWithTiming {
                holidayDiscovery.byCountry(travelLocation.city.country.code, travelLocation.departure, travelLocation.returnDate)
            }
            // Add holidays that happen during the period of the travel
            travelLocation.holidays.addAll( holidays )
            LOGGER.info("Time to get holidays: $timeToGetHolidays ms")
        } catch (e: Exception) {
            LOGGER.severe("Fail to get weather information for ${travelLocation.city}: ${e.message}")
        }
    }

    private fun CoroutineScope.fetchAndUpdatePointOfInterest(travelLocation: TravelLocation, types: List<PointOfInterestType>) = this.launch {
        try {
            pointOfInterestDiscovery.forEach { service ->
                val (poi, timeToGetPOI) = runWithTiming {
                    service.byCoordinateAndType(travelLocation.coordinates, types)
                }
                travelLocation.POI.addAll(poi.takeHighestRatedPlaces(3))
                LOGGER.info("Time to get POI using service ${service::class.java.simpleName}: $timeToGetPOI ms")
            }
        } catch (e: Exception) {
            LOGGER.severe("Fail to get POI information for ${travelLocation.city}: ${e.message}")
        }
    }

    private fun extractTopFlightResults(flightOffers: Array<FlightOfferFromAmadeus>): List<FlightOffer> {
        // Enrich results with choiceProbability
        try {
            val (flightsWithProbability, timeToGetPrediction) = runWithTiming {
                if (flightOffers.isNotEmpty()) {
                    val result = flightOffers.first().response.result
                    amadeus.shopping.flightOffers.prediction
                            .post(result)
                } else {
                    emptyArray<FlightOfferFromAmadeus>()
                }
            }
            LOGGER.info("Time to get Flight Prediction: $timeToGetPrediction ms")

            return flightsWithProbability.takeMostProbableFlights(3)
                    .extractFlightOffers()

        } catch (e: Exception) {
            LOGGER.severe("Error enriching flights with choice probability: ${e.message}")
            throw e
        }
    }

    private fun fetchTopFlightResults(travelLocation: TravelLocation): List<FlightOffer> {
        try {
            val (flightOffers, timeToGetFlightOffers) = runWithTiming {
                amadeus.shopping.flightOffers
                        .get(Params.with("origin", travelLocation.origin)
                                .and("destination", travelLocation.destination)
                                .and("departureDate", travelLocation.departure.format(DateTimeFormatter.ISO_DATE))
                                .and("returnDate", travelLocation.returnDate.format(DateTimeFormatter.ISO_DATE)))
            }
            LOGGER.info("Time to get Flight Offers: $timeToGetFlightOffers ms")

            return extractTopFlightResults(flightOffers)

        } catch (e: Exception) {
            LOGGER.severe("Fail to get flight information for ${travelLocation.origin}: ${e.message}")
            throw Exception("Fail to get flight information for ${travelLocation.origin}: ${e.message}")
        }
    }

    override fun basedOn(origin: String, start: LocalDate, end: LocalDate, theme: Theme): List<TravelLocation> {

        // 1. Get the list of destinations based on Origin
        val startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        val endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val (flightDestinations, timeToGetDestinations) = runWithTiming {
            amadeus.shopping.flightDestinations
                    .get(Params.with("origin", origin))
                    .filter {
                        (it.departureDate in startDate..endDate)
                    }
        }
        LOGGER.info("Time to get destinations: $timeToGetDestinations ms")

        val (travelLocations, timeToGetLocationsAndExtras) = runWithTiming {

            flightDestinations.mapNotNull {

                // Avoiding status code 429: https://github.com/amadeus4dev/developer-guides/issues/6
                Thread.sleep(150 /*ms*/)

                val (travelSuggestion, timeToProcessTimeSuggestion) = runWithTimingOptional {

                    val subType = it.response.result.get("dictionaries").asJsonObject.get("locations").asJsonObject.get(it.destination).asJsonObject.get("subType").asString
                    val (locations, timeToGetLocations) = runWithTiming {
                        amadeus.referenceData.locations.get(Params
                                .with("keyword", it.destination)
                                .and("subType", subType))
                    }
                    LOGGER.info("Time to get Locations for ${it.destination}: $timeToGetLocations ms")

                    if (locations.isEmpty())
                        null
                    else {
                        val locationDetails = locations.first()
                        val zoneOffset = ZoneId.ofOffset("UTC", ZoneOffset.of(locationDetails.timeZoneOffset
                                ?: "+00:00"))
                        val locationAddress = locationDetails.address
                        val coordinate = Coordinate(locationDetails.geoCode.latitude, locationDetails.geoCode.longitude)
                        val city = if (locationAddress != null)
                            City(Country.of(locationAddress.countryCode, locationAddress.countryName), locationAddress.cityName)
                        else
                            City.UNKNOWN

                        val travelSuggestion = TravelLocation(
                                city,
                                it.origin,
                                it.destination,
                                coordinate,
                                it.departureDate.toInstant().atZone(zoneOffset).toLocalDate(),
                                it.returnDate.toInstant().atZone(zoneOffset).toLocalDate(),
                                it.price.total)

                        // Start the process of enriching location with characteristics like Weather, Holidays and Point of Interest.
                        // It's a long-running process that can be executed in parallel, at least for different API providers. If we
                        // try to also parallelize the calls to the same API provider we ran the risk of either flooding the API provider or
                        // just receiving a 429 status code to prevent the flooding
                        runBlocking {
                            coroutineScope {

                                fetchWeatherInformation(travelSuggestion)

                                fetchAndUpdateHolidays(travelSuggestion)

                                fetchAndUpdatePointOfInterest(travelSuggestion, theme.filter.POI.toList())

                            }
                        }

                        return@runWithTimingOptional travelSuggestion
                    }
                }
                LOGGER.info("Time to process destination ${travelSuggestion?.city?.toString() ?: "<Null>"}: $timeToProcessTimeSuggestion ms")

                return@mapNotNull travelSuggestion
            }
        }

        LOGGER.info("Time to get locations and extras: $timeToGetLocationsAndExtras ms")
        LOGGER.info("Total time for this process : ${(timeToGetDestinations + timeToGetLocationsAndExtras) / 1000} seconds \n\n")

        // 3. Filter out cities that don't match the filter criteria or add warnings in case of Holidays
        // TODO: It's better if a domain object does this operation
        val matchingLocations = travelLocations.filter { theme.matches(it).also { if (!it.passed) println(it.reasonToRemove) }.passed }

        matchingLocations.forEach {
            // 4. Sort list based on the AI prediction API from Amadeus
            it.flights.addAll(fetchTopFlightResults(it))
        }

        return matchingLocations
    }

    /***
     * Return a list with the top <amount> flight offers, showing most probable offers first.
     */
    fun Array<FlightOfferFromAmadeus>.takeMostProbableFlights(amount: Int): List<FlightOfferFromAmadeus> {
        return toList()
                .sortedByDescending { it.choiceProbability }
                .take(amount)
    }

    fun List<PointOfInterest>.takeHighestRatedPlaces(amount: Int): List<PointOfInterest> {
        return this.sortedByDescending { it.rating ?: 0 }
                .take(amount)
    }

    fun List<FlightOfferFromAmadeus>.extractFlightOffers(): List<FlightOffer> {
        return this.map { flightOffer ->

            // TODO: Recheck assumptions regarding the use of the collections offerItems, services, and flightSegments
            val price = flightOffer.offerItems.first().price.total

            val inboundService = flightOffer.offerItems.first().services[ServiceType.INBOUND_FLIGHT]
            val inboundDepartureTime = ZonedDateTime.parse(inboundService.segments.first().flightSegment.departure.at)
            val inboundArrivalTime = ZonedDateTime.parse(inboundService.segments.first().flightSegment.arrival.at)
            val inboundFlightDuration = Duration.between(inboundDepartureTime, inboundArrivalTime)
            val inboundAirline = inboundService.segments.first().flightSegment.carrierCode

            val outboundService = flightOffer.offerItems.first().services[ServiceType.OUTBOUND_FLIGHT]
            val outboundDepartureTime = ZonedDateTime.parse(outboundService.segments.first().flightSegment.departure.at)
            val outboundArrivalTime = ZonedDateTime.parse(outboundService.segments.first().flightSegment.arrival.at)
            val outboundFlightDuration = Duration.between(outboundDepartureTime, outboundArrivalTime)
            val outboundAirline = outboundService.segments.first().flightSegment.carrierCode

            FlightOffer(inboundAirline, inboundDepartureTime.format(DateTimeFormatter.ofPattern("dd-MMM HH:mm")), inboundFlightDuration.toHumanReadableFormat(),
                    outboundAirline, outboundDepartureTime.format(DateTimeFormatter.ofPattern("dd-MMM HH:mm")), outboundFlightDuration.toHumanReadableFormat(), price)
        }
    }

    fun Duration.toHumanReadableFormat(): String {
        var result = ""
        var value = Duration.ofMillis(this.toMillis())
        if (value.toDays() > 0) {
            result = "${value.toDays()}d "
            value = value.minusDays(value.toDays())
        }
        result += String.format("%02d", value.toHours())
        value = value.minusHours(value.toHours())

        result += ":${String.format("%02d", value.toMinutes())}"

        return result
    }

    object ServiceType {
        const val INBOUND_FLIGHT = 0
        const val OUTBOUND_FLIGHT = 1
    }

    companion object {
        private val formatter = SimpleDateFormat("yyyy-MM-dd")
        private val LOGGER = Logger.getLogger(DestinySelectionAmadeus::class.java.name)
    }

}
