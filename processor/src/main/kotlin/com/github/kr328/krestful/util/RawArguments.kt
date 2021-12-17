package com.github.kr328.krestful.util

import com.github.kr328.krestful.model.RawArgument
import com.github.kr328.krestful.model.Types
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ksp.toTypeName

fun KSValueParameter.collectRawArgument(): RawArgument {
    val name = name!!.asString()
    val type = type.toTypeName()
    val annotations = collectAnnotations()

    return RawArgument(
        name,
        type,
        annotations[Types.Header],
        annotations[Types.Query],
        annotations[Types.Field],
        annotations[Types.Path],
        annotations[Types.Body],
        annotations[Types.Outgoing],
    )
}