package com.github.kr328.krestful.model

sealed class HttpMethod {
    object GET : HttpMethod()
    object POST : HttpMethod()
    object PUT : HttpMethod()
    object PATCH : HttpMethod()
    object DELETE : HttpMethod()

    data class Custom(val value: String) : HttpMethod()
}
