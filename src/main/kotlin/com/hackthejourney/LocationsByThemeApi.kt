package com.hackthejourney

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LocationsByThemeApi

fun main(args: Array<String>) {
	runApplication<LocationsByThemeApi>(*args)
}
