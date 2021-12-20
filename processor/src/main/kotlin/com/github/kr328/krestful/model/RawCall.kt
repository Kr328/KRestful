package com.github.kr328.krestful.model

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName

data class RawCall(
    val name: String,
    val modifiers: Set<KModifier>,
    val arguments: List<RawArgument>,
    val returning: TypeName,
    val get: Annotation?,
    val post: Annotation?,
    val put: Annotation?,
    val patch: Annotation?,
    val delete: Annotation?,
    val webSocket: Annotation?,
)
