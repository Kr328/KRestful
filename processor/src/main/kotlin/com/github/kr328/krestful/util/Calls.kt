package com.github.kr328.krestful.util

import com.github.kr328.krestful.model.*
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.*

fun RawCall.toCall(resolver: Resolver): Call {
    val annotations = listOfNotNull(get, post, put, patch, delete, request)
    if (annotations.size != 1) {
        error("Invalid method annotations: $annotations")
    }

    val annotation = annotations.single()
    val method = when (annotation.type) {
        Types.GET -> HttpMethod.GET
        Types.POST -> HttpMethod.POST
        Types.PUT -> HttpMethod.PUT
        Types.PATCH -> HttpMethod.PATCH
        Types.DELETE -> HttpMethod.DELETE
        Types.Request -> HttpMethod.Custom(annotation.values["method"] as String)
        else -> error("unreachable")
    }
    val path = annotation.values["path"] as String

    if (returning.isNullable || (returning.className == Types.Flow && (returning as ParameterizedTypeName).typeArguments[0].isNullable)) {
        error("Should not return nullable")
    }

    val isWebSocket = webSocket != null

    if (isWebSocket && returning.className != Types.Flow) {
        error("@WebSocket should return Flow<?>")
    }

    if (!isWebSocket && returning.className == Types.Flow) {
        error("Only @WebSocket can return Flow<?>")
    }

    if (!modifiers.contains(KModifier.SUSPEND) && returning.className != Types.Flow) {
        error("Methods should be suspend or returning Flow<?>")
    }

    if (isWebSocket && (returning as ParameterizedTypeName).typeArguments[0].isNullable) {
        error("Return parameter of Flow<?> should not be nullable")
    }

    val args = arguments.map(RawArgument::toArgument)

    val invalidArgs = args.filterNot {
        val rawType = it.type.className.copy(nullable = false) as ClassName

        when (it.descriptor) {
            Argument.Descriptor.Body -> {
                !isWebSocket && (rawType == Types.TextContent || rawType == Types.ByteArrayContent || rawType.isSerializable(
                    resolver
                ))
            }
            Argument.Descriptor.Outgoing -> {
                isWebSocket && rawType == Types.Flow && !(it.type as ParameterizedTypeName).typeArguments[0].isNullable
            }
            is Argument.Descriptor.Field -> {
                !isWebSocket && (rawType == STRING || rawType.isSerializable(resolver))
            }
            is Argument.Descriptor.Header -> {
                rawType == STRING
            }
            is Argument.Descriptor.Path -> {
                !it.type.isNullable && rawType == STRING
            }
            is Argument.Descriptor.Query -> {
                rawType == STRING
            }
        }
    }
    if (invalidArgs.isNotEmpty()) {
        error("Invalid parameter(s): $invalidArgs")
    }

    val bodyArgs = args.filter { it.descriptor == Argument.Descriptor.Body }
    val fieldArgs = args.filter { it.descriptor is Argument.Descriptor.Field }
    if (bodyArgs.size > 1) {
        error("Duplicate @Body parameter: $bodyArgs")
    }
    if (fieldArgs.isNotEmpty() && bodyArgs.isNotEmpty()) {
        error("@Field should not use with @Body: ${bodyArgs + fieldArgs}")
    }

    when (val rawType = returning.className.copy(nullable = false) as ClassName) {
        UNIT, Types.TextContent, Types.ByteArrayContent, Types.HttpResponse -> Unit
        Types.Flow -> {
            when (val innerType = (returning as ParameterizedTypeName).typeArguments[0]) {
                UNIT, STRING, BYTE_ARRAY, Types.Frame -> Unit
                else -> {
                    if (!innerType.className.isSerializable(resolver)) {
                        error("Unserializable returning: $returning")
                    }
                }
            }
        }
        else -> {
            if (!rawType.isSerializable(resolver)) {
                error("Unserializable returning: $returning")
            }
        }
    }

    val paths = path.parsePath()
    val placeholders = args.mapNotNull { if (it.descriptor is Argument.Descriptor.Path) it.descriptor.key else null }
    paths.segments.forEach { segment ->
        if (segment is UrlTemplate.Segment.Placeholder) {
            if (!placeholders.contains(segment.key)) {
                error("Unknown placeholder: ${segment.key}")
            }
        }
    }

    return Call(
        name,
        modifiers,
        args,
        returning,
        method,
        paths,
        isWebSocket
    )
}