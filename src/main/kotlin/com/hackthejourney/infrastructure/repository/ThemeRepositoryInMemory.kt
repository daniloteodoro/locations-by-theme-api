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
            Theme("Beach", "Get destinations suitable for going to the beach.", "https://images.unsplash.com/photo-1515238152791-8216bfdf89a7?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1052&q=80",
                    TravelFilter(Optional.empty(), Optional.of(20), Optional.of(40), setOf(CLEAR_DAY), setOf(BEACH))
            ),
            Theme("Camping", "", "https://images.unsplash.com/photo-1487750404521-0bc4682c48c5?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1050&q=80",
                    TravelFilter(Optional.of(30), Optional.of(5), Optional.of(40), emptySet(), setOf(CAMPING, PARK))
            ),
            Theme("Climbing", "", "https://images.unsplash.com/photo-1516903150729-a7c4d5cf070d?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=328&q=80",
                    TravelFilter(Optional.of(32), Optional.empty(), Optional.of(20), emptySet(), setOf(CLIMBING))
            ),
            Theme("Cycling", "", "https://images.unsplash.com/photo-1471506480208-91b3a4cc78be?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1053&q=80",
                    TravelFilter(Optional.of(32), Optional.of(5), Optional.of(25), setOf(CLEAR_DAY), setOf(CYCLING, PARK))
            ),
            Theme("Gliding", "", "http://flyhighhg.com/wp-content/uploads/2018/08/Pauls-Falcon-170.jpg",
                    TravelFilter(Optional.empty(), Optional.of(10), Optional.of(10), setOf(CLEAR_DAY), setOf(GLIDING))
            ),
            Theme("Running", "", "https://images.unsplash.com/photo-1518214598173-1666bc921d66?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=376&q=80",
                    TravelFilter(Optional.of(30), Optional.of(5), Optional.of(30), emptySet(), setOf(PARK))
            ),
            Theme("Diving", "", "https://images.unsplash.com/photo-1454442124827-b7de573f10e0?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1051&q=80",
                    TravelFilter(Optional.empty(), Optional.of(20), Optional.empty(), emptySet(), setOf(DIVING))
            ),
            Theme("Skiing", "Get destinations suitable for skiing.", "https://images.unsplash.com/photo-1535640597419-853d35e6364f?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=375&q=80",
                    TravelFilter(Optional.of(8), Optional.empty(), Optional.of(30), setOf(SNOWY), setOf(SKIING))
            )
    )

    override fun all() =
        themes

    override fun byName(themeName: String) =
            themes.firstOrNull { it.name.equals(themeName, true) }

}
