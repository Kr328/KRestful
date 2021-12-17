package com.github.kr328.krestful.util

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec

fun FileSpec.Builder.addSuppress(vararg suppress: String): FileSpec.Builder {
    val annotation = AnnotationSpec.builder(Suppress::class)
        .addMember(suppress.indices.joinToString(",") { "%S" }, *suppress)
        .build()

    return addAnnotation(annotation)
}