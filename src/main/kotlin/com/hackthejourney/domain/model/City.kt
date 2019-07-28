package com.hackthejourney.domain.model

data class Country private constructor(
        val code: String,
        val name: String
) {
    override fun toString(): String {
        return name
    }

    companion object {
        fun of(code: String, name: String): Country {
            val countryName =
                    if (code.equals("us", true))
                        "USA"
                    else
                        name.take(14)
            return Country(code, countryName)
        }
    }
}

data class City (
        val country: Country,
        val name: String
) {
    override fun toString(): String {
        return "$country - $name"
    }

    companion object {
        val UNKNOWN = City(Country.of("", "Unknown"), "Unknown")
    }
}
