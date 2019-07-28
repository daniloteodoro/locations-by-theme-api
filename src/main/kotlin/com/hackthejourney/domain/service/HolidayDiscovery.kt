package com.hackthejourney.domain.service

import com.hackthejourney.domain.model.Holiday
import java.time.LocalDate

interface HolidayDiscovery {

    fun byCountry(country: String, start: LocalDate, end: LocalDate): List<Holiday>

}
