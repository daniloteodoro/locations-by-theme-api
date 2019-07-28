package com.hackthejourney.domain.service

import com.hackthejourney.domain.model.PointOfInterest
import com.hackthejourney.domain.model.PointOfInterestType

interface PointOfInterestDiscovery {

    fun byCoordinateAndType(coordinate: Coordinate, types: List<PointOfInterestType>): List<PointOfInterest>

}
