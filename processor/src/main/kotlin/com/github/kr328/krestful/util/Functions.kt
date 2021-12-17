package com.github.kr328.krestful.util

import com.github.kr328.krestful.Processor
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.annotation.processing.Generated

fun KSClassDeclaration.getAllFunctionsExceptAny(): Sequence<KSFunctionDeclaration> = sequence {
    yieldAll(getDeclaredFunctions())

    superTypes.mapNotNull { it.resolve().declaration as? KSClassDeclaration }.forEach {
        yieldAll(it.getAllFunctionsExceptAny())
    }
}

inline fun FunSpec.Builder.addCode(block: CodeBlock.Builder.() -> Unit) =
    addCode(buildCodeBlock(block))

fun FunSpec.Builder.addParameter(name: String, type: TypeName, defaultValue: String, vararg args: Any) =
    addParameter(
        ParameterSpec.builder(name, type)
            .defaultValue(defaultValue, *args)
            .build()
    )

fun FunSpec.Builder.addGenerated() = apply {
    val annotation = AnnotationSpec.builder(Generated::class)
        .addMember(
            "value = [%S], date = %S",
            Processor::class.qualifiedName!!,
            DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now(Clock.systemUTC()))
        )

    addAnnotation(annotation.build())
}