package com.github.kr328.krestful

import io.ktor.http.*
import io.ktor.http.content.*

class ResponseException(
    val status: HttpStatusCode,
    val content: ByteArrayContent? = null,
    cause: Exception? = null,
) : Exception(cause) {
    override fun toString(): String {
        return "ResponseException{status = $status, content = $content}: $cause"
    }
}