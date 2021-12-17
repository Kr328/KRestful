package com.github.kr328.krestful.model

import com.squareup.kotlinpoet.TypeName

data class RawArgument(
    val name: String,
    val type: TypeName,
    val header: Annotation?,
    val query: Annotation?,
    val field: Annotation?,
    val path: Annotation?,
    val body: Annotation?,
    val outgoing: Annotation?,
)
