package com.github.kr328.krestful.client

import com.github.kr328.krestful.util.ifNullableArgument
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

fun CodeBlock.Builder.addQueryArgument(name: String, type: TypeName, key: String) {
    ifNullableArgument(name, type) {
        addStatement("url.parameters.append(%S, %N)", key, name)
    }
}