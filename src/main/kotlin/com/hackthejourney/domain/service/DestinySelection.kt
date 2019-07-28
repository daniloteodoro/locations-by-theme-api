package com.hackthejourney.domain.service

import com.hackthejourney.domain.model.Theme
import com.hackthejourney.domain.model.TravelLocation
import java.time.LocalDate

interface DestinySelection {

    fun basedOn(origin: String, start: LocalDate, end: LocalDate, theme: Theme): List<TravelLocation>

}
