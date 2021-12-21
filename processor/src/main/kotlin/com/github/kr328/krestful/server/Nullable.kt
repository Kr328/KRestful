package com.github.kr328.krestful.server

import com.github.kr328.krestful.common.Members
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.buildCodeBlock

fun TypeName.ifNullableEnforce(): CodeBlock {
    return buildCodeBlock {
        if (!isNullable) {
            add(".%M()", Members.enforceNotNull)
        }
    }
}
