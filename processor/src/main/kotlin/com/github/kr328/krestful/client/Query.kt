package com.github.kr328.krestful.client

import com.squareup.kotlinpoet.CodeBlock

fun CodeBlock.Builder.addQuery(name: String, key: String) {
    addStatement("query(%S, %L)", key, name)
}