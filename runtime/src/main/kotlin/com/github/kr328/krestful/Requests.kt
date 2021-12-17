package com.github.kr328.krestful

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

data class RequestScope(
    var request: HttpRequestBuilder.() -> Unit,
    var outgoing: () -> Flow<Frame>,
) {
    constructor() : this(
        { error("not implement") },
        { error("not implement") },
    )

    fun request(block: HttpRequestBuilder.() -> Unit) {
        this.request = block
    }

    fun outgoing(block: () -> Flow<Frame>) {
        this.outgoing = block
    }
}

suspend inline fun HttpClient.request(
    baseUrl: Url,
    context: CoroutineContext,
    block: RequestScope.() -> Unit,
): HttpResponse {
    val holder = RequestScope().apply(block)

    return withContext(context) {
        request {
            url {
                takeFrom(baseUrl)
            }

            holder.request(this)
        }
    }
}

inline fun HttpClient.webSocket(
    baseUrl: Url,
    context: CoroutineContext,
    block: RequestScope.() -> Unit,
): Flow<Frame> {
    val holder = RequestScope().apply(block)

    return flow {
        webSocket(
            request = {
                url {
                    takeFrom(baseUrl)
                }

                holder.request(this)

                url {
                    protocol = if (protocol.isSecure()) {
                        URLProtocol.WSS
                    } else {
                        URLProtocol.WS
                    }
                }
            }
        ) {
            launch {
                holder.outgoing().collect {
                    send(it)
                }
            }

            emitAll(incoming)
        }
    }.flowOn(context).onEach { it.readBytes() }
}
