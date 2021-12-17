package com.github.kr328.krestful.client

import com.github.kr328.krestful.model.Argument
import com.github.kr328.krestful.model.Call
import com.github.kr328.krestful.model.Members
import com.github.kr328.krestful.model.Types
import com.github.kr328.krestful.util.controlFlow
import com.github.kr328.krestful.util.ifNullableArgument
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.STRING

fun CodeBlock.Builder.buildOutgoing(call: Call) {
    val outgoing = call.arguments.singleOrNull { it.descriptor == Argument.Descriptor.Outgoing }
    if (outgoing == null) {
        addStatement("%M()", Members.emptyFlow)
    } else {
        ifNullableArgument(outgoing.name, outgoing.type) {
            controlFlow("%N.%M", outgoing.name, Members.map) {
                when (val type = (outgoing.type as ParameterizedTypeName).typeArguments[0]) {
                    STRING -> {
                        addStatement("%T(it)", Types.FrameText)
                    }
                    BYTE_ARRAY -> {
                        addStatement("%T(false, it)", Types.FrameBinary)
                    }
                    else -> {
                        addStatement("json.encodeToString(%T.serializer(), it)", type)
                    }
                }
            }

            next {
                addStatement("%M()", Members.emptyFlow)
            }
        }
    }
}