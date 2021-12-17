package com.github.kr328.krestful.util

import com.github.kr328.krestful.model.RawCall
import com.github.kr328.krestful.model.Types
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName

fun KSFunctionDeclaration.collectRawCall(): RawCall {
    val name = simpleName.asString()
    val modifiers = modifiers.mapNotNull { it.toKModifier() }.toSet()
    val arguments = parameters.map { it.collectRawArgument() }
    val returning = (returnType?.resolve())?.toTypeName() ?: UNIT
    val annotations = collectAnnotations()

    return RawCall(
        name,
        modifiers,
        arguments,
        returning,
        annotations[Types.GET],
        annotations[Types.POST],
        annotations[Types.PUT],
        annotations[Types.PATCH],
        annotations[Types.DELETE],
        annotations[Types.Request],
        annotations[Types.WebSocket],
    )
}