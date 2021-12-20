package com.github.kr328.krestful.server

import com.github.kr328.krestful.common.mappingBlock
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

fun CodeBlock.Builder.addField(name: String, type: TypeName, key: String) {
    addStatement("val %N = field(%S, %L)%L", name, key, type.mappingBlock(), type.ifNullableEnforce())
}