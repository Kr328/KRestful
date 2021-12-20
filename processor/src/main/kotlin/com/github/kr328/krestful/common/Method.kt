package com.github.kr328.krestful.common

import com.github.kr328.krestful.model.HttpMethod
import com.github.kr328.krestful.model.Types
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock

fun HttpMethod.methodBlock(): CodeBlock {
    val format = when (this) {
        HttpMethod.DELETE -> "%T.Delete"
        HttpMethod.GET -> "%T.Get"
        HttpMethod.PATCH -> "%T.Patch"
        HttpMethod.POST -> "%T.Post"
        HttpMethod.PUT -> "%T.Put"
        HttpMethod.WebSocket -> error("Unsupported http method: ${this}")
    }

    return buildCodeBlock {
        add(format, Types.HttpMethod)
    }
}