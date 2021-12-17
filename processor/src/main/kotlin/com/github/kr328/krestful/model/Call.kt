package com.github.kr328.krestful.model

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName

data class Call(
    val name: String,
    val modifiers: Set<KModifier>,
    val arguments: List<Argument>,
    val returning: TypeName,
    val method: HttpMethod,
    val path: UrlTemplate,
    val isWebSocket: Boolean,
)
