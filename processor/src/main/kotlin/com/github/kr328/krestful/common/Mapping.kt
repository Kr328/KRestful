package com.github.kr328.krestful.common

import com.github.kr328.krestful.util.className
import com.squareup.kotlinpoet.*

val TypeName.mappingCode: CodeBlock
    get() = buildCodeBlock {
        when (val clazz = className.copy(nullable = false)) {
            UNIT -> add("%T.Unit", Types.Mapping)
            STRING -> add("%T.String", Types.Mapping)
            BYTE_ARRAY -> add("%T.ByteArray", Types.Mapping)
            INT -> add("%T.Int", Types.Mapping)
            LONG -> add("%T.Long", Types.Mapping)
            FLOAT -> add("%T.Float", Types.Mapping)
            DOUBLE -> add("%T.Double", Types.Mapping)
            BOOLEAN -> add("%T.Boolean", Types.Mapping)
            Types.ContentText -> add("%T.ContentText", Types.Mapping)
            Types.ContentBinary -> add("%T.ContentBinary", Types.Mapping)
            else -> add("%T.SerializableJson(json, %T.serializer())", Types.Mapping, clazz)
        }
    }
