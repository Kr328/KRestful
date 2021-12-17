package com.github.kr328.krestful

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess

suspend fun main() = coroutineScope {
    val client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    val clash = client.createExampleApiProxy(
        Url("http://localhost:6170"),
        json = Json {
            ignoreUnknownKeys = true
        }
    )

    try {
        println(clash.ping().text)
        println(clash.proxies())
        println(clash.proxy("DIRECT"))

        clash.traffic().collect {
            println(it)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        exitProcess(0)
    }
}
