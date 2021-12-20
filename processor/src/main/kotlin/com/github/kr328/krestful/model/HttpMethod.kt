package com.github.kr328.krestful.model

sealed class HttpMethod {
    object GET : HttpMethod()
    object POST : HttpMethod()
    object PUT : HttpMethod()
    object PATCH : HttpMethod()
    object DELETE : HttpMethod()
    object WebSocket : HttpMethod()
}
