package com.github.kr328.krestful.client

import com.github.kr328.krestful.model.Call
import com.github.kr328.krestful.model.Members
import com.github.kr328.krestful.model.Types
import com.github.kr328.krestful.util.className
import com.github.kr328.krestful.util.controlFlow
import com.squareup.kotlinpoet.*

fun CodeBlock.Builder.buildResult(call: Call) {
    val type = if (call.isWebSocket) {
        (call.returning as ParameterizedTypeName).typeArguments[0]
    } else {
        call.returning.className
    }

    when (type) {
        UNIT -> Unit
        Types.HttpResponse, Types.Frame -> addStatement("it")
        BYTE_ARRAY -> {
            controlFlow("when (it)") {
                controlFlow("is %T, is %T ->", Types.FrameText, Types.FrameBinary) {
                    addStatement("it.data")
                }
                controlFlow("else ->") {
                    addStatement("error(\"Unsupported frame: \$it\")")
                }
            }
        }
        Types.ByteArrayContent -> {
            addStatement("%T(it.%M(), it.%M())", Types.ByteArrayContent, Members.readBytes, Members.contentType)
        }
        STRING -> {
            controlFlow("when (it)") {
                controlFlow("is %T, is %T ->", Types.FrameText, Types.FrameBinary) {
                    addStatement("it.data.%M()", Members.decodeString)
                }
                controlFlow("else ->") {
                    addStatement("error(\"Unsupported frame: \$it\")")
                }
            }
        }
        Types.TextContent -> {
            addStatement(
                "%T(it.%M(), it.%M() ?: %T.Any)",
                Types.TextContent,
                Members.readText,
                Members.contentType,
                Types.ContentType
            )
        }
        else -> {
            if (call.isWebSocket) {
                controlFlow("when (it)") {
                    controlFlow("is %T, is %T ->", Types.FrameText, Types.FrameBinary) {
                        addStatement(
                            "json.decodeFromString(%T.serializer(), it.data.%M())",
                            type,
                            Members.decodeString
                        )
                    }
                    controlFlow("else ->") {
                        addStatement("error(\"Unsupported frame: \$it\")")
                    }
                }
            } else {
                controlFlow(
                    "if (it.%M()?.withoutParameters() != %T.Application.Json)",
                    Members.contentType,
                    Types.ContentType
                ) {
                    addStatement("error(\"Unsupported Content-Type: \${it.%M()}\")", Members.contentType)
                }

                addStatement("json.decodeFromString(%T.serializer(), it.%M())", type, Members.readText)
            }
        }
    }
}