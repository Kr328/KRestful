package com.github.kr328.krestful.client

import com.github.kr328.krestful.model.Argument
import com.github.kr328.krestful.model.Call
import com.github.kr328.krestful.model.Types
import com.github.kr328.krestful.util.className
import com.squareup.kotlinpoet.*

fun CodeBlock.Builder.buildRequest(call: Call) {
    val placeholders = call.arguments
        .filter { it.descriptor is Argument.Descriptor.Path }
        .associate { (it.descriptor as Argument.Descriptor.Path).key to it.name }

    addPathArgument(call.path, placeholders)

    addMethodArgument(call.method)

    call.arguments.forEach {
        when (it.descriptor) {
            Argument.Descriptor.Body ->
                addBodyArgument(it.name, it.type)
            is Argument.Descriptor.Header ->
                addHeaderArgument(it.name, it.type, it.descriptor.key)
            is Argument.Descriptor.Query ->
                addQueryArgument(it.name, it.type, it.descriptor.key)
            Argument.Descriptor.Outgoing, is Argument.Descriptor.Field, is Argument.Descriptor.Path -> Unit
        }
    }

    val fields = call.arguments.filter { it.descriptor is Argument.Descriptor.Field }
    if (fields.isNotEmpty()) {
        addFieldsArgument(fields)
    }

    when (call.returning.className) {
        UNIT, Types.TextContent, Types.ByteArrayContent, Types.HttpResponse -> Unit
        Types.Flow -> {
            when ((call.returning as ParameterizedTypeName).typeArguments[0]) {
                UNIT, STRING, BYTE_ARRAY, Types.Frame -> Unit
                else -> {
                    addAcceptJson()
                }
            }
        }
        else -> {
            addAcceptJson()
        }
    }
}
