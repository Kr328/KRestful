package com.github.kr328.krestful.util

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

class ControlFlowScope(val builder: CodeBlock.Builder) {
    inline fun next(format: String, vararg args: Any, block: () -> Unit) {
        builder.nextControlFlow(format, *args)

        block()
    }
}

class IfNullableArgumentScope(val builder: CodeBlock.Builder) {
    inline fun next(block: () -> Unit) {
        builder.nextControlFlow("else")

        block()
    }
}

inline fun CodeBlock.Builder.controlFlow(format: String, vararg args: Any, block: ControlFlowScope.() -> Unit) {
    beginControlFlow(format, *args)

    ControlFlowScope(this).block()

    endControlFlow()
}

inline fun CodeBlock.Builder.ifNullableArgument(
    name: String,
    type: TypeName,
    block: IfNullableArgumentScope.() -> Unit
) {
    if (type.isNullable) {
        beginControlFlow("if (%N != null)", name)
    }

    IfNullableArgumentScope(this).block()

    if (type.isNullable) {
        endControlFlow()
    }
}

