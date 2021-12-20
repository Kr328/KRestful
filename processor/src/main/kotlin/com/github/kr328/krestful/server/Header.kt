package com.github.kr328.krestful.server

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

fun CodeBlock.Builder.addHeader(name: String, type: TypeName, key: String) {
    addStatement("val %N = header(%S)%L", name, key, type.ifNullableEnforce())
}