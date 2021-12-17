package com.github.kr328.krestful.util

import com.github.kr328.krestful.model.Types
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName

fun ClassName.isSerializable(resolver: Resolver): Boolean {
    val clazz = resolver.getClassDeclarationByName(this.toString()) ?: return false

    return clazz.annotations.any {
        it.annotationType.toTypeName() == Types.Serializable
    }
}