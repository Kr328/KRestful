package com.github.kr328.krestful.client

import com.github.kr328.krestful.common.mappingCode
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

fun CodeBlock.Builder.addBody(name: String, type: TypeName) {
    addStatement("body(%N, %L)", name, type.mappingCode)
}
