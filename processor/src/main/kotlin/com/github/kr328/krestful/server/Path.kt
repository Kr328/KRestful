package com.github.kr328.krestful.server

import com.squareup.kotlinpoet.CodeBlock

fun CodeBlock.Builder.addPath(name: String, key: String) {
    addStatement("val %N = path(%S)", name, key)
}
