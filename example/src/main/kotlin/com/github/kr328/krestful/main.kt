package com.github.kr328.krestful

import com.github.kr328.krestful.model.Configs
import com.github.kr328.krestful.model.Proxy
import com.github.kr328.krestful.model.Traffic
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess
import io.ktor.websocket.WebSockets as SWebSockets

suspend fun main() = coroutineScope {
    embeddedServer(Netty, port = 10880) {
        install(SWebSockets)

        routing {
            withExampleApiDelegate(object : ExampleApi {
                private var configs = Configs(
                    port = 8080,
                    socksPort = 1080,
                    redirPort = 1088,
                    tproxyPort = 1089,
                    mixedPort = 1088,
                    allowLan = true,
                    mode = "Rule",
                    logLevel = "Debug",
                    ipv6 = false,
                )

                override suspend fun ping(): String {
                    return "Hello"
                }

                override suspend fun getConfigs(): Configs {
                    return configs
                }

                override suspend fun patchConfigs(configs: Configs) {
                    this.configs = configs
                }

                override suspend fun proxies(): Proxy.All {
                    return Proxy.All(mapOf("DIRECT" to Proxy("DIRECT", "", 0, "114514")))
                }

                override suspend fun proxy(name: String): Proxy {
                    if (name == "DIRECT") {
                        return Proxy("DIRECT", "", 0, "1919810")
                    }

                    throw ResponseException(HttpStatusCode.NotFound)
                }

                override fun traffic(): Flow<Traffic> {
                    return flow {
                        repeat(10) {
                            emit(Traffic(114, 514))

                            delay(1000L)
                        }
                    }
                }

                override suspend fun createProxy(name: String, port: Int): Proxy {
                    return Proxy(name, "localhost", port, "password")
                }

                override suspend fun headers(example: String, name: String): String {
                    return "$example:$name"
                }

                override suspend fun queries(name: String, server: String): String {
                    val call = currentCoroutineContext()[ApplicationCall]!!

                    println(call.request.uri)

                    return "$name/$server"
                }

                override fun echo(input: Flow<String>): Flow<String> {
                    return input
                }

                override suspend fun nullable(query: String?, header: String?, field: String?): String {
                    return "$query/$header/$field"
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

        println(api.getConfigs())

        api.patchConfigs(api.getConfigs().copy(port = 0))

        println(api.getConfigs())

        println(api.proxies())

        println(api.proxy("DIRECT"))

        println(api.createProxy("GLOBAL", 114514))

        println(api.headers("114514", "REJECT"))

        println(api.queries("DEBUG", "localhost"))

        println(api.nullable(null, null, null))

        api.traffic().collect {
            println(it)
        }

        api.echo(flowOf("aaaaa", "bbbbbb", "cccccc", "ddddddd")).collect {
            println(it)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        exitProcess(0)
    }
}
