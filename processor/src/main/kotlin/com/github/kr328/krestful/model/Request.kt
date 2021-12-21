package com.github.kr328.krestful.model

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName

data class Request(
    val name: String,
    val modifiers: Set<KModifier>,
    val arguments: List<Argument>,
    val returning: TypeName,
    val method: Method,
    val path: Path,
) {
    data class Raw(
        val name: String,
        val modifiers: Set<KModifier>,
        val arguments: List<Argument.Raw>,
        val returning: TypeName,
        val get: Annotation?,
        val post: Annotation?,
        val put: Annotation?,
        val patch: Annotation?,
        val delete: Annotation?,
        val webSocket: Annotation?,
    )

    enum class Method {
        GET, POST, PUT, PATCH, DELETE, WebSocket
    }
}
