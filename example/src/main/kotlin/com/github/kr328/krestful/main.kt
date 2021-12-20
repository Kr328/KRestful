package com.github.kr328.krestful

import com.github.kr328.krestful.model.Proxy
import com.github.kr328.krestful.model.Traffic
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.system.exitProcess

suspend fun main() = coroutineScope {
    embeddedServer(Netty, port = 10880) {
        install(io.ktor.websocket.WebSockets)

        routing {
            withExampleApiDelegate(object : ExampleApi {
                override suspend fun json(): String {
                    return "json"
                }

                override suspend fun ping(): String {
                    return "114514"
                }

                override fun traffic(): Flow<Traffic> {
                    return emptyFlow()
                }

                override suspend fun stub(name: String, obj: JsonObject) {

                }

                override suspend fun proxies(): Proxy.All {
                    return Proxy.All(emptyMap())
                }

                override suspend fun proxy(name: String): Proxy {
                    return Proxy(name)
                }
            })
        }
    }.start()

    val client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    val api = client.createExampleApiProxy(
        Url("http://localhost:10880"),
        json = Json {
            ignoreUnknownKeys = true
        }
    )

    try {
        println(api.ping())
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        exitProcess(0)
    }
}
