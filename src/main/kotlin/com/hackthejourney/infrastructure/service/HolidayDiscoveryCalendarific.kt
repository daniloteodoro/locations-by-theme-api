package com.hackthejourney.infrastructure.service

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.hackthejourney.application.DependencyError
import com.hackthejourney.application.controller.HolidayApiResponse
import com.hackthejourney.domain.model.Holiday
import com.hackthejourney.domain.service.HolidayDiscovery
import com.hackthejourney.infrastructure.json.fromJson
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class HolidayDiscoveryCalendarific(
        @Value("\${calendarific.url}") private val calendarificUrl: String,
        @Value("\${HOLIDAY_API_KEY}") private val holidayApiKey: String) : HolidayDiscovery {

    override fun byCountry(country: String, start: LocalDate, end: LocalDate): List<Holiday> {

        // TODO: Handle cases where period crosses years
        // TODO: If service does not respond on time or error we can still continue, but showing an additional warning as: Cannot retrieve holidays
        val (_, _, result) = calendarificUrl.httpGet(listOf("api_key" to holidayApiKey,"country" to country, "year" to start.year))

                // Make sure service responds on time
                .timeoutRead(1500)

                .responseString()

        val jsonResult = when (result) {
            is Result.Failure -> {
                 throw DependencyError("Error fetching holidays from $country: " + result.getException().message)
            }
            is Result.Success -> {
                result.get()
            }
        }

        if (jsonResult.isBlank())
            return emptyList()

        val holidayList = jsonResult.fromJson<HolidayApiResponse>().response.holidays

        return holidayList.filter { it.date.toLocalDate() in start..end &&
                                    it.type.contains("National holiday")}
                        .map { Holiday(country, it.name, it.date.toLocalDate() ) }
    }

}
