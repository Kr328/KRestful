package com.github.kr328.krestful.util

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName

val TypeName.className: ClassName
    get() {
        return when (this) {
            is ClassName -> this
            is ParameterizedTypeName -> rawType
            else -> error("Unknown type $this")
        }
    }
