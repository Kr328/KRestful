package com.github.kr328.krestful.client

import com.github.kr328.krestful.model.Members
import com.github.kr328.krestful.model.UrlTemplate
import com.squareup.kotlinpoet.CodeBlock

fun CodeBlock.Builder.addPathArgument(url: UrlTemplate, placeholders: Map<String, String>) {
    addStatement("url.%M(\"%L\")", Members.pathComponents, url.segments.joinToString("") {
        when (it) {
            is UrlTemplate.Segment.Literal -> {
                it.value
            }
            is UrlTemplate.Segment.Placeholder -> {
                "\${${placeholders[it.key]!!}}"
            }
        }
    })
}