package com.github.kr328.krestful.client

import com.github.kr328.krestful.common.mappingCode
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

fun CodeBlock.Builder.addField(name: String, type: TypeName, key: String) {
    addStatement("field(%S, %N, %L)", key, name, type.mappingCode)
}
