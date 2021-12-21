package com.github.kr328.krestful.common

import com.github.kr328.krestful.util.className
import com.github.kr328.krestful.util.isSerializable
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.*

fun TypeName.enforceNotNullable(name: String) {
    when (this) {
        is ClassName -> {
            require(!isNullable) {
                "$name should not be nullable"
            }
        }
        is ParameterizedTypeName -> {
            require(!isNullable) {
                "$name should not be nullable"
            }

            typeArguments.forEach {
                it.enforceNotNullable(name)
            }
        }
        else -> Unit
    }
}

fun TypeName.enforceMappable(name: String, resolver: Resolver, ignoreNullable: Boolean = true) {
    val type = if (ignoreNullable) {
        copy(nullable = false)
    } else {
        this
    }

    val valid = when (val raw = type.className) {
        UNIT,
        INT, LONG,
        FLOAT, DOUBLE,
        BOOLEAN,
        STRING, BYTE_ARRAY,
        Types.ContentText, Types.ContentBinary -> true
        else -> raw.isSerializable(resolver)
    }

    require(valid) {
        "$name should be mappable type: $this"
    }
}

inline fun TypeName.compareClassName(
    type: TypeName,
    ignoreNullable: Boolean,
    block: (TypeName, TypeName) -> Unit
) {
    val (a, b) = if (ignoreNullable) {
        this.copy(nullable = false) to type.copy(nullable = false)
    } else {
        this to type
    }

    block(a.className, b.className)
}

fun TypeName.enforceClassName(name: String, type: TypeName, ignoreNullable: Boolean = true) {
    compareClassName(type, ignoreNullable) { a, b ->
        require(a == b) {
            "$name should be $type"
        }
    }
}

fun TypeName.enforceNotClassName(name: String, type: TypeName, ignoreNullable: Boolean = true) {
    compareClassName(type, ignoreNullable) { a, b ->
        require(a != b) {
            "$name should not be $type"
        }
    }
}