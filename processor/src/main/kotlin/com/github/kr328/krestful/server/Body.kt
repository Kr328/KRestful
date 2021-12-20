package com.github.kr328.krestful.server

import com.github.kr328.krestful.common.mappingBlock
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

fun CodeBlock.Builder.addBody(name: String, type: TypeName) {
    addStatement("val %N = body(%L)%L", name, type.mappingBlock(), type.ifNullableEnforce())
}
