package com.github.kr328.krestful.server

import com.github.kr328.krestful.model.Path
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock

val Path.urlCode: CodeBlock
    get() = buildCodeBlock {
        val text = joinToString("") {
            when (it) {
                is Path.Segment.Literal -> it.value
                is Path.Segment.Variable -> "{${it.key}}"
            }
        }
        add("%S", text)
    }