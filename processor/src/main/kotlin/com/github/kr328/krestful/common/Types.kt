package com.github.kr328.krestful.common

import com.squareup.kotlinpoet.ClassName

object Types {
    val Restful = ClassName("com.github.kr328.krestful.annotations", "Restful")
    val GET = ClassName("com.github.kr328.krestful.annotations", "GET")
    val POST = ClassName("com.github.kr328.krestful.annotations", "POST")
    val PUT = ClassName("com.github.kr328.krestful.annotations", "PUT")
    val PATCH = ClassName("com.github.kr328.krestful.annotations", "PATCH")
    val DELETE = ClassName("com.github.kr328.krestful.annotations", "DELETE")
    val WebSocket = ClassName("com.github.kr328.krestful.annotations", "WebSocket")
    val Header = ClassName("com.github.kr328.krestful.annotations", "Header")
    val Field = ClassName("com.github.kr328.krestful.annotations", "Field")
    val Query = ClassName("com.github.kr328.krestful.annotations", "Query")
    val Path = ClassName("com.github.kr328.krestful.annotations", "Path")
    val Body = ClassName("com.github.kr328.krestful.annotations", "Body")
    val Outgoing = ClassName("com.github.kr328.krestful.annotations", "Outgoing")
    val Mapping = ClassName("com.github.kr328.krestful.internal", "Mapping")
    val ContentText = ClassName("com.github.kr328.krestful", "Content", "Text")
    val ContentBinary = ClassName("com.github.kr328.krestful", "Content", "Binary")
    val Flow = ClassName("kotlinx.coroutines.flow", "Flow")
    val Dispatchers = ClassName("kotlinx.coroutines", "Dispatchers")
    val CoroutineContext = ClassName("kotlin.coroutines", "CoroutineContext")
    val Url = ClassName("io.ktor.http", "Url")
    val Route = ClassName("io.ktor.routing", "Route")
    val HttpClient = ClassName("io.ktor.client", "HttpClient")
    val HttpMethod = ClassName("io.ktor.http", "HttpMethod")
    val Serializable = ClassName("kotlinx.serialization", "Serializable")
    val Json = ClassName("kotlinx.serialization.json", "Json")
}
