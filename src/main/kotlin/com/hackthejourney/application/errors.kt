package com.hackthejourney.application

import com.hackthejourney.domain.ReasonToTravelException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class LocationNotFound(msg: String) : ReasonToTravelException(msg)

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class DependencyError(msg: String) : ReasonToTravelException(msg)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidTheme(msg: String) : ReasonToTravelException(msg)
