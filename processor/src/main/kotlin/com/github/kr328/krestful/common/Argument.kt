package com.github.kr328.krestful.common

import com.github.kr328.krestful.model.Argument
import com.github.kr328.krestful.util.collectAnnotations
import com.github.kr328.krestful.util.ifNullOrEmpty
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ksp.toTypeName

fun KSValueParameter.collectArgument(): Argument.Raw {
    val name = name!!.asString()
    val type = type.toTypeName()
    val annotations = collectAnnotations()

    return Argument.Raw(
        name,
        type,
        annotations[Types.Header],
        annotations[Types.Query],
        annotations[Types.Field],
        annotations[Types.Path],
        annotations[Types.Body],
        annotations[Types.Outgoing],
    )
}

fun Argument.Raw.refine(): Argument {
    val annotations = listOfNotNull(header, field, body, query, path)
    require(annotations.size == 1) {
        "Duplicate or empty argument descriptors: $annotations"
    }

    val annotation = annotations.single()
    val descriptor = when (annotation.type) {
        Types.Header -> {
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