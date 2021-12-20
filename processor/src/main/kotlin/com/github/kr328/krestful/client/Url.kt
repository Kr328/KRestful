package com.github.kr328.krestful.client

import com.github.kr328.krestful.model.UrlTemplate
import com.github.kr328.krestful.model.UrlTemplate.Segment.Literal
import com.github.kr328.krestful.model.UrlTemplate.Segment.Variable
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock

fun UrlTemplate.urlBlock(): CodeBlock {
    return buildCodeBlock {
        val text = segments.joinToString("") {
            when (it) {
                is Literal -> it.value
                is Variable -> "\${%N}"
            }
        }
        add(text, *segments.filterIsInstance<Variable>().map(Variable::name).toTypedArray())
    }
}
