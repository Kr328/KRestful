package com.github.kr328.krestful.client

import com.github.kr328.krestful.model.Path
import com.github.kr328.krestful.model.Path.Segment.Literal
import com.github.kr328.krestful.model.Path.Segment.Variable
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock

val Path.templateCode: CodeBlock
    get() = buildCodeBlock {
        val text = joinToString("", prefix = "\"", postfix = "\"") {
            when (it) {
                is Literal -> it.value
                is Variable -> "\${%N}"
            }
        }
        add(text, *filterIsInstance<Variable>().map(Variable::name).toTypedArray())
    }
