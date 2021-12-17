package com.github.kr328.krestful.client

import com.github.kr328.krestful.model.Types
import com.github.kr328.krestful.util.className
import com.github.kr328.krestful.util.ifNullableArgument
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

fun CodeBlock.Builder.addBodyArgument(name: String, type: TypeName) {
    ifNullableArgument(name, type) {
        when (val clazz = type.className.copy(nullable = false)) {
            Types.TextContent, Types.ByteArrayContent ->
                addStatement("body = %N", name)
            else ->
                addStatement(
                    "body = %T(json.encodeToString(%T.serializer(), %N), %T.Application.Json)",
                    Types.TextContent,
                    clazz,
                    name,
                    Types.ContentType
                )
        }
    }
}