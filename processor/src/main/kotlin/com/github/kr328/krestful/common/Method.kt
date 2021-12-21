package com.github.kr328.krestful.common

import com.github.kr328.krestful.model.Request
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock

val Request.Method.gettingCode: CodeBlock
    get() {
        val format = when (this) {
            Request.Method.DELETE -> "%T.Delete"
            Request.Method.GET -> "%T.Get"
            Request.Method.PATCH -> "%T.Patch"
            Request.Method.POST -> "%T.Post"
            Request.Method.PUT -> "%T.Put"
            Request.Method.WebSocket -> error("Unsupported http method: $this")
        }

        return buildCodeBlock {
            add(format, Types.HttpMethod)
        }
    }