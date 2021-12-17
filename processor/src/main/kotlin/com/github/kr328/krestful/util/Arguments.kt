package com.github.kr328.krestful.util

import com.github.kr328.krestful.model.Argument
import com.github.kr328.krestful.model.RawArgument
import com.github.kr328.krestful.model.Types
import com.squareup.kotlinpoet.STRING

fun RawArgument.toArgument(): Argument {
    val annotations = listOfNotNull(header, field, body, query, path)
    if (annotations.size != 1) {
        error("Invalid method parameter descriptors: $annotations")
    }

    val annotation = annotations.single()
    val descriptor = when (annotation.type) {
        Types.Header -> {
            require(type != STRING) {
                "Invalid header parameter type: $type, requires String"
            }

            Argument.Descriptor.Header(
                (annotation.values["key"] as? String).ifNullOrEmpty { name }
            )
        }
        Types.Query -> {
            Argument.Descriptor.Query(
                (annotation.values["key"] as? String).ifNullOrEmpty { name }
            )
        }
        Types.Field -> {
            Argument.Descriptor.Field(
                (annotation.values["key"] as? String).ifNullOrEmpty { name }
            )
        }
        Types.Path -> {
            Argument.Descriptor.Path(
                (annotation.values["placeholder"] as? String).ifNullOrEmpty { name }
            )
        }
        Types.Body -> {
            Argument.Descriptor.Body
        }
        Types.Outgoing -> {
            Argument.Descriptor.Outgoing
        }
        else -> error("unreachable")
    }

    return Argument("__${name}", type, descriptor)
}