package com.github.kr328.krestful.server

import com.github.kr328.krestful.common.mappingCode
import com.github.kr328.krestful.util.typeArguments
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

fun CodeBlock.Builder.addOutgoing(name: String, type: TypeName) {
    addStatement("val %N = incoming(%L)%L", name, type.typeArguments[0].mappingCode, type.ifNullableEnforce())
}