package com.github.kr328.krestful.util

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ksp.toClassName

fun KSClassDeclaration.getAllFunctionsExceptAny(): Sequence<KSFunctionDeclaration> = sequence {
    if (toClassName() != ANY) {
        yieldAll(getDeclaredFunctions())

        superTypes.mapNotNull { it.resolve().declaration as? KSClassDeclaration }.forEach {
            yieldAll(it.getAllFunctionsExceptAny())
        }
    }
}