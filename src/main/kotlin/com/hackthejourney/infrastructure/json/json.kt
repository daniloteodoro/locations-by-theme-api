package com.hackthejourney.infrastructure.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.readValue

fun Any.toJson(): String {
    try {
        val objMapper = ObjectMapper()
        objMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE;
        return objMapper.writeValueAsString(this)
    } catch (e: Exception) {
        throw RuntimeException("Failure converting object to JSON: ${e.message}")
    }
}

inline fun <reified T> String.fromJson(propertyNamingStrategy: PropertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE): T {
    try {
        val objMapper = ObjectMapper()
        objMapper.propertyNamingStrategy = propertyNamingStrategy;
        objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objMapper.readValue<T>(this)
    } catch (e: Exception) {
        throw RuntimeException("Failure converting JSON to object of class ${T::class.java}: ${e.message}")
    }
}
