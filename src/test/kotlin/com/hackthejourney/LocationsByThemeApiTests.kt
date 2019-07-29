package com.hackthejourney

import com.hackthejourney.domain.model.*
import com.hackthejourney.domain.service.Coordinate
import com.hackthejourney.domain.service.WeatherInfo
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class LocationsByThemeApiTests {

	private val beachFilter = TravelFilter(Optional.empty(), Optional.of(20), Optional.of(40), setOf(WeatherStatus.CLEAR_DAY), setOf(PointOfInterestType.BEACH))
	private val maxTempOnlyFilter = TravelFilter(Optional.of(20), Optional.empty(), Optional.empty(), emptySet(), emptySet())

	@Test
	fun contextLoads() {
	}

	@Test
	fun testLocationDoesNotContainWeather() {
		val aTheme = Theme("", "", "", beachFilter)
		val aLocation = TravelLocation(City.UNKNOWN, "", "", Coordinate(0.0, 0.0), LocalDate.now(), LocalDate.now(), 100.0)

		val result = aTheme.matches(aLocation)

		Assert.assertFalse(result.passed)
	}

	@Test
	fun testLocationContainsCorrectMaxTemperature() {
		val aTheme = Theme("", "", "", maxTempOnlyFilter)
		val aLocation = TravelLocation(City.UNKNOWN, "", "", Coordinate(0.0, 0.0), LocalDate.now(), LocalDate.now(), 100.0)
		aLocation.weather = WeatherInfo.of(LocalDateTime.now(), 15, -2, null, null, WeatherStatus.SNOWY)

		val result = aTheme.matches(aLocation)

		Assert.assertTrue(result.passed)
	}

	@Test
	fun testLocationDoesNotContainCorrectMaxTemperature() {
		val aTheme = Theme("", "", "", maxTempOnlyFilter)
		val aLocation = TravelLocation(City.UNKNOWN, "", "", Coordinate(0.0, 0.0), LocalDate.now(), LocalDate.now(), 100.0)
		aLocation.weather = WeatherInfo.of(LocalDateTime.now(), 35, 22, null, null, WeatherStatus.CLEAR_DAY)

		val result = aTheme.matches(aLocation)

		Assert.assertFalse(result.passed)
	}

}
