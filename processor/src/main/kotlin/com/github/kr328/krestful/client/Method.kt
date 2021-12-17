package com.github.kr328.krestful.client

import com.github.kr328.krestful.model.HttpMethod
import com.github.kr328.krestful.model.Types
import com.squareup.kotlinpoet.CodeBlock

fun CodeBlock.Builder.addMethodArgument(method: HttpMethod) {
    when (method) {
        is HttpMethod.Custom -> {
            addStatement("method = %T(%S)", Types.HttpMethod, method.value)
        }
        HttpMethod.DELETE -> {
            addStatement("method = %T.Delete", Types.HttpMethod)
        }
        HttpMethod.GET -> {
            addStatement("method = %T.Get", Types.HttpMethod)
        }
        HttpMethod.PATCH -> {
            addStatement("method = %T.Patch", Types.HttpMethod)
        }
        HttpMethod.POST -> {
            addStatement("method = %T.Post", Types.HttpMethod)
        }
        HttpMethod.PUT -> {
            addStatement("method = %T.Put", Types.HttpMethod)
        }
    }
}