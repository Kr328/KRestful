package com.github.kr328.krestful.util

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

fun KSClassDeclaration.getAllFunctionsExceptAny(): Sequence<KSFunctionDeclaration> = sequence {
    yieldAll(getDeclaredFunctions())

    superTypes.mapNotNull { it.resolve().declaration as? KSClassDeclaration }.forEach {
        yieldAll(it.getAllFunctionsExceptAny())
    }
}