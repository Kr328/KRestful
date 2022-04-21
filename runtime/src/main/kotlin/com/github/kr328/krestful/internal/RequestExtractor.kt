package com.github.kr328.krestful.internal

import com.github.kr328.krestful.Calling
import com.github.kr328.krestful.Content
import com.github.kr328.krestful.RemoteException
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*

class RequestExtractor(
    private val call: ApplicationCall,
    private val incoming: Flow<Frame>?,
) {
    private var fields: Map<String, JsonElement>? = null

    fun path(key: String): String {
        return call.parameters[key]!!
    }

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
            val contentType = call.request.contentType().withoutParameters()

            if (contentType != ContentType.Application.Json && contentType != ContentType.Any) {
                throw SerializationException("Unsupported Content-Type: $contentType")
            }

            fields = Json.Default.decodeFromString(JsonObject.serializer(), call.receiveText())
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

fun <T> T?.enforceNotNull(): T {
    return this ?: throw RemoteException(HttpStatusCode.BadRequest)
}

fun <T> Route.withRequest(
    path: String,
    method: HttpMethod,
    result: Mapping<T>,
    block: suspend RequestExtractor.() -> T
) {
    route(path, method) {
        handle {
            withContext(Calling(call)) {
                try {
                    val response = when (val r = result.mappingToContent(RequestExtractor(call, null).block())) {
                        is Content.Binary -> ByteArrayContent(r.bytes, r.contentType)
                        is Content.Text -> TextContent(r.text, r.contentType)
                    }

                    call.respond(response)
                } catch (e: RemoteException) {
                    call.respondRemoteException(e)
                }
            }
        }
    }
}

fun <T> Route.withWebSocket(
    path: String,
    result: Mapping<T>,
    block: RequestExtractor.() -> Flow<T>
) {
    webSocket(path) {
        withContext(Calling(call)) {
            try {
                RequestExtractor(call, incoming.consumeAsFlow())
                    .block()
                    .collect {
                        val frame = when (val c = result.mappingToContent(it)) {
                            is Content.Binary -> Frame.Binary(true, c.bytes)
                            is Content.Text -> Frame.Text(c.text)
                        }

                        send(frame)
                    }
            } catch (e: RemoteException) {
                call.respondRemoteException(e)
            }
        }
    }
}

private suspend fun ApplicationCall.respondRemoteException(e: RemoteException) {
    val body = Json.Default.encodeToString(
        JsonObject.serializer(),
        JsonObject(e.extras.mapValues { JsonPrimitive(it.value) })
    )

    respond(
        e.status,
        TextContent(
            body,
            ContentType.Application.Json.withParameter(
                CONTENT_TYPE_PARAMETER_CLASS_NAME,
                RemoteException::class.java.name
            )
        )
    )
}