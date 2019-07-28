package com.hackthejourney.application.controller

import com.fasterxml.jackson.annotation.JsonIgnore
import com.hackthejourney.application.dto.TravelApiResponse
import com.hackthejourney.domain.model.Holiday
import com.hackthejourney.domain.service.HolidayDiscovery
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters.firstDayOfMonth
import java.time.temporal.TemporalAdjusters.lastDayOfMonth

@CrossOrigin
@RestController
@RequestMapping("/holidays/{country}", produces = [MediaType.APPLICATION_JSON_VALUE])
@Api(value = "holidays", tags = ["Holidays"], description = "List holidays based on a given location and period.")
class HolidayController(private val holidayDiscovery: HolidayDiscovery) {

    @ApiOperation("Return holidays in the origin (country code such as NL or US) that happen in the current month, by default. You can change start and end period to match your intended travel time.")
    @GetMapping
    fun searchHolidays(@PathVariable country: String,
                       @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") start: LocalDate?,
                       @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") end: LocalDate?): ResponseEntity<TravelApiResponse<List<Holiday>>> {

        val startPeriod = start ?: LocalDate.now().with(firstDayOfMonth())
        val endPeriod = end ?: LocalDate.now().with(lastDayOfMonth())

        return ResponseEntity.ok(
                TravelApiResponse(holidayDiscovery.byCountry(country, startPeriod, endPeriod))
        )
    }

}

data class HolidayApiHolidayDate(
        val iso: String = ""
) {
    @JsonIgnore
    fun toLocalDate(): LocalDate {
        return LocalDate.parse(iso.take(10))
    }

}

data class HolidayApiHoliday(
        val name: String = "",
        val date: HolidayApiHolidayDate = HolidayApiHolidayDate(),
        val type: Array<String> = emptyArray()
)

data class HolidayApiHolidays(
        val holidays: Array<HolidayApiHoliday> = emptyArray()
)

data class HolidayApiResponse(
        val response: HolidayApiHolidays = HolidayApiHolidays()
)
