package com.hackthejourney.infrastructure.repository

import com.hackthejourney.domain.model.PointOfInterestType.*
import com.hackthejourney.domain.model.Theme
import com.hackthejourney.domain.model.TravelFilter
import com.hackthejourney.domain.model.WeatherStatus.*
import com.hackthejourney.domain.repository.ThemeRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ThemeRepositoryInMemory : ThemeRepository {

    private val themes = listOf(
            Theme("Beach", "Get destinations suitable for going to the beach.", "http://localhost:8080/img/themes/Beach.png",
                    TravelFilter(Optional.empty(), Optional.of(20), Optional.of(40), setOf(CLEAR_DAY), setOf(BEACH))
            ),
            Theme("Camping", "Get destinations suitable for camping", "http://localhost:8080/img/themes/Camping.png",
                    TravelFilter(Optional.of(30), Optional.of(5), Optional.of(40), emptySet(), setOf(CAMPING, PARK))
            ),
            Theme("Climbing", "Get destinations suitable for climbing", "http://localhost:8080/img/themes/Climbing.png",
                    TravelFilter(Optional.of(32), Optional.empty(), Optional.of(20), emptySet(), setOf(CLIMBING))
            ),
            Theme("Cycling", "Get destinations suitable for cycling", "http://localhost:8080/img/themes/cycling.png",
                    TravelFilter(Optional.of(32), Optional.of(5), Optional.of(25), setOf(CLEAR_DAY), setOf(CYCLING, PARK))
            ),
            Theme("Gliding", "Get destinations suitable for hang-gliding, paragliding, etc", "http://localhost:8080/img/themes/Gliding.png",
                    TravelFilter(Optional.empty(), Optional.of(10), Optional.of(10), setOf(CLEAR_DAY), setOf(GLIDING))
            ),
            Theme("Running", "Get destinations suitable for running", "http://localhost:8080/img/themes/running.png",
                    TravelFilter(Optional.of(30), Optional.of(5), Optional.of(30), emptySet(), setOf(PARK))
            ),
            Theme("Diving", "Get destinations suitable for diving", "http://localhost:8080/img/themes/Diving.png",
                    TravelFilter(Optional.empty(), Optional.of(20), Optional.empty(), emptySet(), setOf(DIVING))
            ),
            Theme("Skiing", "Get destinations suitable for skiing.", "http://localhost:8080/img/themes/skiing.jpg",
                    TravelFilter(Optional.of(8), Optional.empty(), Optional.of(30), emptySet(),/* setOf(SNOWY), <- Kind of hard to find */setOf(SKIING))
            )
    )

    override fun all() =
        themes

    override fun byName(themeName: String) =
            themes.firstOrNull { it.name.equals(themeName, true) }

}
