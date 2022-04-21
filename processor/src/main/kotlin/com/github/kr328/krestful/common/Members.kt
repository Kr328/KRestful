package com.github.kr328.krestful.common

import com.squareup.kotlinpoet.MemberName

object Members {
    val route = MemberName("io.ktor.server.routing", "route", true)
    val appendPathSegments = MemberName("io.ktor.http", "appendPathSegments", true)
    val request = MemberName("com.github.kr328.krestful.internal", "request", true)
    val webSocket = MemberName("com.github.kr328.krestful.internal", "webSocket", true)
    val withRequest = MemberName("com.github.kr328.krestful.internal", "withRequest", true)
    val withWebSocket = MemberName("com.github.kr328.krestful.internal", "withWebSocket", true)
    val enforceNotNull = MemberName("com.github.kr328.krestful.internal", "enforceNotNull", true)
}
