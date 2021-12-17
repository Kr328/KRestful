package com.github.kr328.krestful.model

import com.squareup.kotlinpoet.MemberName

object Members {
    val request = MemberName("com.github.kr328.krestful", "request", true)
    val webSocket = MemberName("com.github.kr328.krestful", "webSocket", true)
    val pathComponents = MemberName("io.ktor.http", "pathComponents", true)
    val readText = MemberName("io.ktor.client.statement", "readText", true)
    val readBytes = MemberName("io.ktor.client.statement", "readBytes", true)
    val accept = MemberName("io.ktor.client.request", "accept", true)
    val contentType = MemberName("io.ktor.http", "contentType", true)
    val emptyFlow = MemberName("kotlinx.coroutines.flow", "emptyFlow")
    val let = MemberName("kotlin", "let", true)
    val map = MemberName("kotlinx.coroutines.flow", "map", true)
    val decodeString = MemberName("kotlin.text", "decodeToString", true)
}
