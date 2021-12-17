package com.github.kr328.krestful.client

import com.github.kr328.krestful.model.Argument
import com.github.kr328.krestful.model.Types
import com.github.kr328.krestful.util.className
import com.github.kr328.krestful.util.controlFlow
import com.github.kr328.krestful.util.ifNullableArgument
import com.squareup.kotlinpoet.*

fun CodeBlock.Builder.addFieldsArgument(fields: List<Argument>) {
    controlFlow("val _body = sequence") {
        for (field in fields) {
            val key = (field.descriptor as Argument.Descriptor.Field).key

            ifNullableArgument(field.name, field.type) {
                when (val type = field.type.className.copy(nullable = false)) {
                    SHORT, INT, LONG, STRING, FLOAT, DOUBLE, BOOLEAN, BYTE -> {
                        addStatement("yield(%S to %T(%N))", key, Types.JsonPrimitive, field.name)
                    }
                    else -> {
                        addStatement("yield(%S to json.encodeToJsonElement(%T.serializer(), %N))", key, type, field.name)
                    }
                }
            }
        }
    }

    addStatement(".toMap()")

    addStatement(
        "body = %T(json.encodeToString(%T.serializer(), %T(_body)), %T.Application.Json)",
        Types.TextContent,
        Types.JsonObject,
        Types.JsonObject,
        Types.ContentType,
    )
}
