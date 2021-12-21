package com.github.kr328.krestful.internal

import com.github.kr328.krestful.Content
import com.github.kr328.krestful.ResponseException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*

fun <T> T?.enforceNotNull(): T {
    if (this == null) {
        throw ResponseException(HttpStatusCode.BadRequest, cause = NullPointerException())
    }

    return this
}

class ResponseScope(
    val call: ApplicationCall,
    private val incoming: Flow<Frame>?,
    private val json: Json,
) {
    private var fields: Map<String, JsonElement>? = null

    fun header(key: String): String? {
        return call.request.header(key)
    }

    fun query(key: String): String? {
        return call.request.queryParameters[key]
    }

    suspend fun <T> body(mapping: Mapping<T>): T? {
        return mapping.mappingToValue(Content.Binary(call.receive(ByteArray::class), call.request.contentType()))
    }

    fun <T> incoming(mapping: Mapping<T>): Flow<T>? {
        return incoming?.map {
            val content = when (it) {
                is Frame.Binary -> Content.Text(it.data.decodeToString(), call.request.contentType())
                is Frame.Text -> Content.Binary(it.data, call.request.contentType())
                else -> error("Unsupported frame: $it")
            }

            mapping.mappingToValue(content)
        }
    }

    suspend fun <T> field(key: String, mapping: Mapping<T>): T? {
        if (fields == null) {
            val contentType = call.request.contentType()

            if (contentType != ContentType.Application.Json || contentType != ContentType.Any) {
                throw SerializationException()
            }

            fields = json.decodeFromString(JsonObject.serializer(), call.receiveText())
        }

        val element = fields?.get(key) ?: return null

        @Suppress("UNCHECKED_CAST")
        return when (mapping) {
            Mapping.ContentBinary -> {
                val bytes = (element as JsonPrimitive).contentOrNull?.toByteArray() ?: return null

                Content.Binary(bytes, ContentType.Any) as T
            }
            Mapping.ContentText -> {
                val text = (element as JsonPrimitive).contentOrNull ?: return null

                Content.Text(text, ContentType.Any) as T
            }
            Mapping.Boolean -> {
                val content = (element as JsonPrimitive).contentOrNull
                content?.toBooleanStrictOrNull() as? T ?: throw NumberFormatException("Invalid boolean: $content")
            }
            Mapping.ByteArray -> {
                (element as JsonPrimitive).contentOrNull?.toByteArray() as? T
            }
            Mapping.Double -> {
                (element as JsonPrimitive).contentOrNull?.toDouble() as? T
            }
            Mapping.Float -> {
                (element as JsonPrimitive).contentOrNull?.toFloat() as? T
            }
            Mapping.Int -> {
                (element as JsonPrimitive).contentOrNull?.toInt() as? T
            }
            Mapping.Long -> {
                (element as JsonPrimitive).contentOrNull?.toLong() as? T
            }
            Mapping.String -> {
                (element as JsonPrimitive).contentOrNull as? T
            }
            Mapping.Unit -> Unit as T
            is Mapping.SerializableJson -> {
                mapping.json.decodeFromJsonElement(mapping.serializer, element)
            }
        }
    }
}

fun <T> Route.withRequest(
    json: Json,
    method: HttpMethod,
    path: String,
    result: Mapping<T>,
    block: suspend ResponseScope.() -> T
) {
    route(path, method) {
        handle {
            try {
                val response = when (val r = result.mappingToContent(ResponseScope(call, null, json).block())) {
                    is Content.Binary -> ByteArrayContent(r.bytes, r.contentType)
                    is Content.Text -> TextContent(r.text, r.contentType)
                }

                call.respond(response)
            } catch (e: SerializationException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (e: ResponseException) {
                call.respond(e.status)
            }
        }
    }
}

fun <T> Route.withWebSocket(
    json: Json,
    path: String,
    result: Mapping<T>,
    block: ResponseScope.() -> Flow<T>
) {
    webSocket(path) {
        try {
            ResponseScope(call, incoming.consumeAsFlow(), json)
                .block()
                .collect {
                    val frame = when (val c = result.mappingToContent(it)) {
                        is Content.Binary -> Frame.Binary(false, c.bytes)
                        is Content.Text -> Frame.Text(c.text)
                    }

                    send(frame)
                }
        } catch (e: SerializationException) {
            call.respond(HttpStatusCode.BadRequest)
        } catch (e: ResponseException) {
            call.respond(e.status)
        }
    }
}