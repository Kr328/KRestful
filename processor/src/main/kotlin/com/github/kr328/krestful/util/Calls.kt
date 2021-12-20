package com.github.kr328.krestful.util

import com.github.kr328.krestful.model.*
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.*

fun RawCall.toCall(resolver: Resolver): Call {
    val annotations = listOfNotNull(get, post, put, patch, delete, webSocket)
    if (annotations.size != 1) {
        error("Invalid method annotations: $annotations")
    }

    val annotation = annotations.single()
    val method = when (annotation.type) {
        Types.WebSocket -> HttpMethod.WebSocket
        Types.GET -> HttpMethod.GET
        Types.POST -> HttpMethod.POST
        Types.PUT -> HttpMethod.PUT
        Types.PATCH -> HttpMethod.PATCH
        Types.DELETE -> HttpMethod.DELETE
        else -> error("unreachable")
    }
    val path = annotation.values["path"] as String

    if (returning.isNullable || (returning.className == Types.Flow && (returning as ParameterizedTypeName).typeArguments[0].isNullable)) {
        error("Should not return nullable")
    }

    if (method == HttpMethod.WebSocket && returning.className != Types.Flow) {
        error("@WebSocket should return Flow<?>")
    }

    if (method != HttpMethod.WebSocket && returning.className == Types.Flow) {
        error("Only @WebSocket can return Flow<?>")
    }

    if (!modifiers.contains(KModifier.SUSPEND) && returning.className != Types.Flow) {
        error("Methods should be suspend or returning Flow<?>")
    }

    if (method == HttpMethod.WebSocket && (returning as ParameterizedTypeName).typeArguments[0].isNullable) {
        error("Return parameter of Flow<?> should not be nullable")
    }

    val args = arguments.map(RawArgument::toArgument)

    val invalidArgs = args.filterNot {
        val rawType = it.type.className.copy(nullable = false) as ClassName

        when (it.descriptor) {
            Argument.Descriptor.Body, Argument.Descriptor.Outgoing -> {
                if (it.descriptor == Argument.Descriptor.Body && method == HttpMethod.WebSocket) {
                    false
                } else if (it.descriptor == Argument.Descriptor.Outgoing && method != HttpMethod.WebSocket) {
                    false
                } else {
                    when (rawType) {
                        UNIT,
                        INT, LONG,
                        FLOAT, DOUBLE,
                        BOOLEAN,
                        STRING, BYTE_ARRAY,
                        Types.ContentText, Types.ContentBinary -> true
                        else -> rawType.isSerializable(resolver)
                    }
                }
            }
            Argument.Descriptor.Outgoing -> {
                method == HttpMethod.WebSocket && rawType == Types.Flow && !(it.type as ParameterizedTypeName).typeArguments[0].isNullable
            }
            is Argument.Descriptor.Field -> {
                method != HttpMethod.WebSocket && (rawType == STRING || rawType.isSerializable(resolver))
            }
            is Argument.Descriptor.Header -> {
                rawType == STRING
            }
            is Argument.Descriptor.Query -> {
                rawType == STRING
            }
            is Argument.Descriptor.Path -> {
                it.type == STRING
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

    val rawReturn = if (returning.className == Types.Flow) {
        (returning as ParameterizedTypeName).typeArguments[0].className
    } else {
        returning.className
    }
    when (rawReturn) {
        UNIT,
        INT, LONG,
        FLOAT, DOUBLE,
        BOOLEAN,
        STRING, BYTE_ARRAY,
        Types.ContentText, Types.ContentBinary -> Unit
        else -> if (!rawReturn.isSerializable(resolver)) {
            error("Unserializable returning: $returning")
        }
    }

    val pathArguments = args.filter { it.descriptor is Argument.Descriptor.Path }
        .associate { (it.descriptor as Argument.Descriptor.Path).key to it.name }

    return Call(
        name,
        modifiers,
        args,
        returning,
        method,
        path.parsePath(pathArguments),
    )
}