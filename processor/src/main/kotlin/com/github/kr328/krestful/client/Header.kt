package com.github.kr328.krestful.client

import com.squareup.kotlinpoet.CodeBlock

fun CodeBlock.Builder.addHeader(name: String, key: String) {
    addStatement("header(%S, %N)", key, name)
}
