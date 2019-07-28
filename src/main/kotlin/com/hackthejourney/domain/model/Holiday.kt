package com.hackthejourney.domain.model

import java.time.LocalDate

data class Holiday (
        val location: String,
        val name: String,
        val date: LocalDate)
