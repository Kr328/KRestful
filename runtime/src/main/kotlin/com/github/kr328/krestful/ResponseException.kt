package com.github.kr328.krestful

import io.ktor.http.*

class ResponseException(
    val status: HttpStatusCode,
    cause: Exception? = null,
) : Exception(cause) {
    override fun toString(): String {
        return "ResponseException{status = $status}: $cause"
    }
}