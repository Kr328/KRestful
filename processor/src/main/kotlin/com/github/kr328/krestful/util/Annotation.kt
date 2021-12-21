package com.github.kr328.krestful.util

import com.github.kr328.krestful.model.Annotation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName

fun KSAnnotation.toAnnotation(): Annotation {
    val values = arguments.associate { it.name!!.asString() to it.value }

    return Annotation(annotationType.toTypeName().className, values)
}

fun KSAnnotated.collectAnnotations(): Map<ClassName, Annotation> {
    return annotations.associate { it.annotationType.toTypeName().className to it.toAnnotation() }
}
