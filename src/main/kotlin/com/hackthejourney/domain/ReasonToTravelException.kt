package com.hackthejourney.domain


open class ReasonToTravelException(msg: String, cause: Throwable?) : Exception(msg, cause) {

    constructor(msg: String): this(msg, null)

}
