package com.github.kr328.krestful.client

import com.github.kr328.krestful.common.mappingBlock
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName

fun CodeBlock.Builder.addOutgoing(name: String, type: TypeName) {
    val inner = (type as ParameterizedTypeName).typeArguments[0]

    addStatement("outgoing(%N, %L)", name, inner.mappingBlock())
}