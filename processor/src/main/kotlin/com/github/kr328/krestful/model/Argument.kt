package com.github.kr328.krestful.model

import com.squareup.kotlinpoet.TypeName

data class Argument(
    val name: String,
    val type: TypeName,
    val descriptor: Descriptor,
) {
    sealed class Descriptor {
        object Body : Descriptor()
        object Outgoing : Descriptor()

        data class Header(val key: String) : Descriptor()
        data class Query(val key: String) : Descriptor()
        data class Field(val key: String) : Descriptor()
        data class Path(val key: String) : Descriptor()
    }

    data class Raw(
        val name: String,
        val type: TypeName,
        val header: Annotation?,
        val query: Annotation?,
        val field: Annotation?,
        val path: Annotation?,
        val body: Annotation?,
        val outgoing: Annotation?,
    )
}
