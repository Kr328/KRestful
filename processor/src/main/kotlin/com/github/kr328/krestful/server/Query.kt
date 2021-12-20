package com.github.kr328.krestful.server

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

fun CodeBlock.Builder.addQuery(name: String, type: TypeName, key: String) {
    addStatement("val %N = query(%S)%L", name, key, type.ifNullableEnforce())
}