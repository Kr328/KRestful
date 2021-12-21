package com.github.kr328.krestful.util

import com.github.kr328.krestful.common.Types
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

val TypeName.typeArguments: List<TypeName>
    get() = (this as ParameterizedTypeName).typeArguments

val TypeName.className: ClassName
    get() {
        return when (this) {
            is ClassName -> this
            is ParameterizedTypeName -> rawType
            else -> error("Unknown type $this")
        }
    }

fun ClassName.isSerializable(resolver: Resolver): Boolean {
    val clazz = resolver.getClassDeclarationByName(this.toString()) ?: return false

    return clazz.annotations.any {
        it.annotationType.toTypeName() == Types.Serializable
    }
}