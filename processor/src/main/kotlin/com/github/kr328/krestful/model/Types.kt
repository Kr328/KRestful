package com.github.kr328.krestful.model

import com.squareup.kotlinpoet.ClassName

object Types {
    val Restful = ClassName("com.github.kr328.krestful.annotations", "Restful")
    val GET = ClassName("com.github.kr328.krestful.annotations", "GET")
    val POST = ClassName("com.github.kr328.krestful.annotations", "POST")
    val PUT = ClassName("com.github.kr328.krestful.annotations", "PUT")
    val PATCH = ClassName("com.github.kr328.krestful.annotations", "PATCH")
    val DELETE = ClassName("com.github.kr328.krestful.annotations", "DELETE")
    val Request = ClassName("com.github.kr328.krestful.annotations", "Request")
    val WebSocket = ClassName("com.github.kr328.krestful.annotations", "WebSocket")
    val Header = ClassName("com.github.kr328.krestful.annotations", "Header")
    val Field = ClassName("com.github.kr328.krestful.annotations", "Field")
    val Query = ClassName("com.github.kr328.krestful.annotations", "Query")
    val Path = ClassName("com.github.kr328.krestful.annotations", "Path")
    val Body = ClassName("com.github.kr328.krestful.annotations", "Body")
    val Outgoing = ClassName("com.github.kr328.krestful.annotations", "Outgoing")
    val Flow = ClassName("kotlinx.coroutines.flow", "Flow")
    val Url = ClassName("io.ktor.http", "Url")
    val HttpClient = ClassName("io.ktor.client", "HttpClient")
    val HttpResponse = ClassName("io.ktor.client.statement", "HttpResponse")
    val Serializable = ClassName("kotlinx.serialization", "Serializable")
    val Json = ClassName("kotlinx.serialization.json", "Json")
    val JsonPrimitive = ClassName("kotlinx.serialization.json", "JsonPrimitive")
    val JsonObject = ClassName("kotlinx.serialization.json", "JsonObject")
    val CoroutineContext = ClassName("kotlin.coroutines", "CoroutineContext")
    val HttpMethod = ClassName("io.ktor.http", "HttpMethod")
    val Dispatchers = ClassName("kotlinx.coroutines", "Dispatchers")
    val ContentType = ClassName("io.ktor.http", "ContentType")
    val TextContent = ClassName("io.ktor.http.content", "TextContent")
    val ByteArrayContent = ClassName("io.ktor.http.content", "ByteArrayContent")
    val Frame = ClassName("io.ktor.http.cio.websocket", "Frame")
    val FrameText = ClassName("io.ktor.http.cio.websocket", "Frame", "Text")
    val FrameBinary = ClassName("io.ktor.http.cio.websocket", "Frame", "Binary")
}
