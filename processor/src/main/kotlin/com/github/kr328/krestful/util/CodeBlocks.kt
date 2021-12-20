package com.github.kr328.krestful.util

import com.squareup.kotlinpoet.CodeBlock

class ControlFlowScope(val builder: CodeBlock.Builder) {
    inline fun next(format: String, vararg args: Any, block: () -> Unit) {
        builder.nextControlFlow(format, *args)

        block()
    }
}

inline fun CodeBlock.Builder.controlFlow(format: String, vararg args: Any, block: ControlFlowScope.() -> Unit) {
    beginControlFlow(format, *args)

    ControlFlowScope(this).block()

    endControlFlow()
}

