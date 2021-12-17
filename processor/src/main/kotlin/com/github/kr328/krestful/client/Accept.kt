package com.github.kr328.krestful.client

import com.github.kr328.krestful.model.Members
import com.github.kr328.krestful.model.Types
import com.squareup.kotlinpoet.CodeBlock

fun CodeBlock.Builder.addAcceptJson() {
    addStatement("%M(%T.Application.Json)", Members.accept, Types.ContentType)
}