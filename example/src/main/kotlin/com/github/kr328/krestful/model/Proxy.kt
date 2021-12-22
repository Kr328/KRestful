package com.github.kr328.krestful.model

import kotlinx.serialization.Serializable

@Serializable
data class Proxy(
    val name: String,
    val server: String,
    val port: Int,
    val password: String,
) {
    @Serializable
    data class All(val proxies: Map<String, Proxy>)
}
