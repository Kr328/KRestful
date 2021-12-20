@file:Suppress("UNCHECKED_CAST")

package com.github.kr328.krestful.internal

import com.github.kr328.krestful.Content
import com.github.kr328.krestful.ResponseException
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext
import io.ktor.client.features.ResponseException as KResponseException

class RequestScope {
    private val queries: MutableMap<String, String> = mutableMapOf()
    private val headers: MutableMap<String, String> = mutableMapOf()
    private val fields: MutableMap<String, JsonElement> = mutableMapOf()
    private var body: Content? = null
    internal var outgoing: Flow<Content>? = null

    fun header(key: String, value: String) {
        headers[key] = value
    }

    @JvmName("headerNullable")
    fun header(key: String, value: String?) {
        if (value != null) {
            header(key, value)
        }
    }

    fun query(key: String, value: String) {
        queries[key] = value
    }

    @JvmName("queryNullable")
    fun query(key: String, value: String?) {
        if (value != null) {
            query(key, value)
        }
    }

    fun <T> body(value: T, mapping: Mapping<T>) {
        body = mapping.mappingToContent(value)
    }

    @JvmName("bodyNullable")
    fun <T> body(value: T?, mapping: Mapping<T>) {
        if (value != null) {
            body(value, mapping)
        }
    }

    fun <T> outgoing(value: Flow<T>, mapping: Mapping<T>) {
        outgoing = value.map { mapping.mappingToContent(it) }
    }

    @JvmName("outgoingNullable")
    fun <T> outgoing(value: Flow<T>?, mapping: Mapping<T>) {
        if (value != null) {
            outgoing(value, mapping)
        }
    }

    fun <T> field(key: String, value: T, mapping: Mapping<T>) {
        when (mapping) {
            Mapping.BinaryContent -> {
                val content = (value as Content.Binary)
                val charset = content.contentType.charset() ?: Charsets.UTF_8

                fields[key] = JsonPrimitive(content.bytes.toString(charset))
            }
            Mapping.TextContent -> {
                fields[key] = JsonPrimitive((value as Content.Text).text)
            }
            Mapping.String -> {
                fields[key] = JsonPrimitive(value as String)
            }
            Mapping.ByteArray -> {
                fields[key] = JsonPrimitive((value as ByteArray).toString(Charsets.UTF_8))
            }
            Mapping.Boolean -> {
                fields[key] = JsonPrimitive(value as Boolean)
            }
            Mapping.Double -> {
                fields[key] = JsonPrimitive(value as Double)
            }
            Mapping.Float -> {
                fields[key] = JsonPrimitive(value as Float)
            }
            Mapping.Int -> {
                fields[key] = JsonPrimitive(value as Int)
            }
            Mapping.Long -> {
                fields[key] = JsonPrimitive(value as Long)
            }
            Mapping.Unit -> Unit
            is Mapping.SerializableJson -> {
                fields[key] = mapping.json.encodeToJsonElement(mapping.serializer, value)
            }
        }
    }

    @JvmName("fieldNullable")
    fun <T> field(key: String, value: T?, mapping: Mapping<T>) {
        if (value != null) {
            field(key, value, mapping)
        }
    }

    fun applyTo(json: Json, request: HttpRequestBuilder) {
        request.url {
            queries.forEach { (k, v) ->
                parameters[k] = v
            }
        }

        headers.forEach { (k, v) ->
            request.headers[k] = v
        }

        val body = body
        if (body != null) {
            request.body = when (body) {
                is Content.Binary -> ByteArrayContent(body.bytes, body.contentType)
                is Content.Text -> TextContent(body.text, body.contentType)
            }
        } else if (fields.isNotEmpty()) {
            request.body = TextContent(
                json.encodeToString(JsonObject.serializer(), JsonObject(fields)),
                ContentType.Application.Json
            )
        }
    }
}

suspend fun <T> HttpClient.request(
    context: CoroutineContext,
    json: Json,
    url: Url,
    path: String,
    method: HttpMethod,
    returning: Mapping<T>,
    block: suspend RequestScope.() -> Unit
): T {
    return withContext(context) {
        val metadata = RequestScope().apply {
            block()
        }

        try {
            val response: HttpResponse =
                request {
                    this.method = method

                    this.url {
                        takeFrom(url)

                        pathComponents(path)
                    }

                    metadata.applyTo(json, this)

                    if (returning is Mapping.SerializableJson<T>) {
                        accept(ContentType.Application.Json)
                    }
                }

            returning.mappingToValue(Content.Binary(response.readBytes(), response.contentType() ?: ContentType.Any))
        } catch (e: KResponseException) {
            throw ResponseException(
                e.response.status,
                ByteArrayContent(e.response.readBytes(), e.response.contentType(), e.response.status),
                e
            )
        } catch (e: SerializationException) {
            throw ResponseException(HttpStatusCode.NotFound, cause = e)
        } catch (e: NumberFormatException) {
            throw ResponseException(HttpStatusCode.NotFound, cause = e)
        }
    }
}

fun <T> HttpClient.webSocket(
    context: CoroutineContext,
    json: Json,
    url: Url,
    path: String,
    returning: Mapping<T>,
    block: RequestScope.() -> Unit
): Flow<T> {
    return flow {
        try {
            val metadata = RequestScope().apply(block)

            webSocket(request = {
                this.url {
                    takeFrom(url)

                    pathComponents(path)
                }

                metadata.applyTo(json, this)

                if (returning is Mapping.SerializableJson<T>) {
                    accept(ContentType.Application.Json)
                }
            }) {
                try {
                    val out = metadata.outgoing
                    if (out != null) {
                        launch {
                            out.collect {
                                val frame = when (it) {
                                    is Content.Binary -> Frame.Binary(false, it.bytes)
                                    is Content.Text -> Frame.Text(it.text)
                                }

                                send(frame)
                            }
                        }
                    }

                    val contentType = call.response.contentType() ?: ContentType.Any

                    incoming.receiveAsFlow().collect {
                        val content = when (it) {
                            is Frame.Binary -> Content.Binary(it.data, contentType)
                            is Frame.Text -> Content.Text(it.data.toString(Charsets.UTF_8), contentType)
                            else -> error("Unsupported frame: $it")
                        }
                        emit(returning.mappingToValue(content))
                    }
                } finally {
                    cancel()
                }
            }
        } catch (e: KResponseException) {
            throw ResponseException(
                e.response.status,
                ByteArrayContent(e.response.readBytes(), e.response.contentType(), e.response.status),
                e
            )
        } catch (e: SerializationException) {
            throw ResponseException(HttpStatusCode.NotFound, cause = e)
        } catch (e: NumberFormatException) {
            throw ResponseException(HttpStatusCode.NotFound, cause = e)
        }
    }.flowOn(context)
}