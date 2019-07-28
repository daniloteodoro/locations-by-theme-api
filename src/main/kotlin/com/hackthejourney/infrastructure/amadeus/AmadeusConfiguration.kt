package com.hackthejourney.infrastructure.amadeus

import com.amadeus.Amadeus
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.logging.Logger

@Component
class AmadeusConfiguration {

    // Make Amadeus client available as a dependency to other components
    @Bean
    fun getAmadeusClient() =
        Amadeus.builder(System.getenv())    // Initialize client by reading Amadeus API credentials from the env variables
                .setLogger(LOGGER)
                .build()

    companion object {
        private val LOGGER = Logger.getLogger(AmadeusConfiguration::class.java.name)
    }

}
