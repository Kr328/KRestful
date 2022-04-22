@file:Suppress("UNCHECKED_CAST")

package com.github.kr328.krestful.internal

import com.github.kr328.krestful.Content
import com.github.kr328.krestful.RemoteException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.http.content.TextContent
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.collections.set

class RequestBuilder {
    private val queries: MutableList<Pair<String, String>> = mutableListOf()
    private val headers: MutableList<Pair<String, String>> = mutableListOf()
    private var fields: MutableMap<String, JsonElement>? = mutableMapOf()
    private var body: Content? = null
    private var outgoing: Flow<Content>? = null

    fun header(key: String, value: String): RequestBuilder = apply {
        headers.add(key to value)
    }

    @JvmName("headerNullable")
    fun header(key: String, value: String?): RequestBuilder = apply {
        if (value != null) {
            header(key, value)
        }
    }

    fun query(key: String, value: String): RequestBuilder = apply {
        queries.add(key to value)
    }

    @JvmName("queryNullable")
    fun query(key: String, value: String?): RequestBuilder = apply {
        if (value != null) {
            query(key, value)
        }
    }

    fun <T> body(value: T, mapping: Mapping<T>): RequestBuilder = apply {
        body = mapping.mappingToContent(value)
    }

    @JvmName("bodyNullable")
    fun <T> body(value: T?, mapping: Mapping<T>): RequestBuilder = apply {
        if (value != null) {
            body(value, mapping)
        }
    }

    fun <T> outgoing(value: Flow<T>, mapping: Mapping<T>): RequestBuilder = apply {
        outgoing = value.map { mapping.mappingToContent(it) }
    }

    @JvmName("outgoingNullable")
    fun <T> outgoing(value: Flow<T>?, mapping: Mapping<T>): RequestBuilder = apply {
        if (value != null) {
            outgoing(value, mapping)
        }
    }

    fun <T> field(key: String, value: T, mapping: Mapping<T>): RequestBuilder = apply {
        val fields = fields ?: mutableMapOf()

        this.fields = fields

        when (mapping) {
            Mapping.ContentBinary -> {
                fields[key] = JsonPrimitive((value as Content.Binary).bytes.encodeBase64())
            }
            Mapping.ContentText -> {
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
    fun <T> field(key: String, value: T?, mapping: Mapping<T>): RequestBuilder = apply {
        this.fields = fields ?: mutableMapOf()

        if (value != null) {
            field(key, value, mapping)
        }
    }

    fun constructRequest(builder: HttpRequestBuilder) {
        queries.forEach { (key, value) ->
            builder.url.parameters.append(key, value)
        }

        headers.forEach { (key, value) ->
            builder.headers.append(key, value)
        }

        val body = body
        if (body != null) {
            builder.setBody(
                when (body) {
                    is Content.Binary -> ByteArrayContent(body.bytes, body.contentType)
                    is Content.Text -> TextContent(body.text, body.contentType)
                }
            )
        } else {
            val fields = this@RequestBuilder.fields
            if (fields != null) {
                builder.setBody(
                    TextContent(
                        Json.Default.encodeToString(JsonObject.serializer(), JsonObject(fields)),
                        ContentType.Application.Json
                    )
                )
            }
        }
    }

    suspend fun relayTo(session: WebSocketSession) {
        outgoing?.let { outgoing ->
            outgoing.collect {
                val frame = when (it) {
                    is Content.Binary -> Frame.Binary(false, it.bytes)
                    is Content.Text -> Frame.Text(it.text)
                }

                session.send(frame)
            }
        }
    }
}

suspend fun <T> HttpClient.request(
    baseUrl: Url,
    path: String,
    method: HttpMethod,
    returning: Mapping<T>,
    builderBlock: RequestBuilder.() -> Unit,
): T {
    try {
        val builder = RequestBuilder().apply(builderBlock)

        val response = request {
            this.expectSuccess = true
            this.method = method
            this.url.takeFrom(baseUrl).appendPathSegments(path)

            builder.constructRequest(this)

            if (returning is Mapping.SerializableJson<*>) {
                accept(ContentType.Application.Json)
            }
        }

        return returning.mappingToValue(Content.Binary(response.body(), response.contentType() ?: ContentType.Any))
    } catch (e: ResponseException) {
        e.throwRemoteExceptionIfAvailable()

        throw e
    }
}

fun <T> HttpClient.webSocket(
    baseUrl: Url,
    path: String,
    returning: Mapping<T>,
    builderBlock: RequestBuilder.() -> Unit,
): Flow<T> = flow {
    val builder = RequestBuilder().apply(builderBlock)

    webSocket(request = {
        this.expectSuccess = true
        this.method = HttpMethod.Get
        this.url {
            takeFrom(baseUrl).appendPathSegments(path)

            if (!url.protocol.isWebsocket()) {
                if (url.protocol.isSecure()) {
                    url.protocol = URLProtocol.WSS
                } else {
                    url.protocol = URLProtocol.WS
                }
            }
        }

        builder.constructRequest(this)
    }) {
        val session = this

        launch {
            builder.relayTo(session)
        }

        val contentType = call.response.contentType() ?: ContentType.Any

        incoming.consumeAsFlow().collect {
            val content = when (it) {
                is Frame.Binary -> Content.Binary(it.data, contentType)
                is Frame.Text -> Content.Text(it.data.toString(Charsets.UTF_8), contentType)
                else -> error("Unsupported frame: $it")
            }
            emit(returning.mappingToValue(content))
        }
    }
}

private suspend fun ResponseException.throwRemoteExceptionIfAvailable() {
    val contentType = response.contentType() ?: return
    when {
        contentType.withoutParameters() != ContentType.Application.Json -> return
        contentType.parameter(CONTENT_TYPE_PARAMETER_CLASS_NAME) != RemoteException::class.java.name -> return
    }

    val extras = Json.Default.decodeFromString(JsonObject.serializer(), response.bodyAsText())

    throw RemoteException(response.status, extras.mapValues { it.value.toString() })
}