package com.github.kr328.krestful.common

import com.github.kr328.krestful.model.Argument
import com.github.kr328.krestful.model.Request
import com.github.kr328.krestful.util.collectAnnotations
import com.github.kr328.krestful.util.parsePath
import com.github.kr328.krestful.util.typeArguments
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName

fun KSFunctionDeclaration.collectRequest(): Request.Raw {
    val name = simpleName.asString()
    val modifiers = modifiers.mapNotNull { it.toKModifier() }.toSet()
    val arguments = parameters.map { it.collectArgument() }
    val returning = (returnType?.resolve())?.toTypeName() ?: UNIT
    val annotations = collectAnnotations()

    return Request.Raw(
        name,
        modifiers,
        arguments,
        returning,
        annotations[Types.GET],
        annotations[Types.POST],
        annotations[Types.PUT],
        annotations[Types.PATCH],
        annotations[Types.DELETE],
        annotations[Types.WebSocket],
    )
}

fun Request.Raw.refine(resolver: Resolver): Request {
    val annotations = listOfNotNull(get, post, put, patch, delete, webSocket)
    require(annotations.size == 1) {
        "Duplicate or empty request method: $annotations"
    }

    val annotation = annotations.single()
    val method = when (annotation.type) {
        Types.WebSocket -> Request.Method.WebSocket
        Types.GET -> Request.Method.GET
        Types.POST -> Request.Method.POST
        Types.PUT -> Request.Method.PUT
        Types.PATCH -> Request.Method.PATCH
        Types.DELETE -> Request.Method.DELETE
        else -> error("unreachable")
    }
    val path = annotation.values["path"] as String

    returning.enforceNotNullable("return")

    if (method == Request.Method.WebSocket) {
        returning.enforceClassName("return", Types.Flow, ignoreNullable = false)
        returning.typeArguments[0].enforceMappable("return", resolver)

        require(!modifiers.contains(KModifier.SUSPEND)) {
            "WebSocket request should not be suspendable"
        }
    } else {
        returning.enforceNotClassName("return", Types.Flow, ignoreNullable = false)
        returning.enforceMappable("return", resolver)

        require(modifiers.contains(KModifier.SUSPEND)) {
            "Request should be suspendable"
        }
    }

    val args = arguments.map(Argument.Raw::refine).onEach {
        when (it.descriptor) {
            Argument.Descriptor.Body, Argument.Descriptor.Outgoing -> {
                val type = if (method == Request.Method.WebSocket) {
                    require(it.descriptor != Argument.Descriptor.Body) {
                        "WebSocket request should not contains @Body"
                    }

                    it.type.enforceClassName(it.name, Types.Flow)

                    it.type.typeArguments[0]
                } else {
                    require(it.descriptor != Argument.Descriptor.Outgoing) {
                        "Request should not contains @Outgoing"
                    }

                    it.type
                }

                type.enforceMappable(it.name, resolver)
            }
            is Argument.Descriptor.Field -> {
                require(method != Request.Method.WebSocket) {
                    "WebSocket should not contains @Field"
                }

                it.type.enforceMappable(it.name, resolver)
            }
            is Argument.Descriptor.Header -> {
                it.type.enforceClassName(it.name, STRING)
            }
            is Argument.Descriptor.Path -> {
                it.type.enforceClassName(it.name, STRING, ignoreNullable = false)
            }
            is Argument.Descriptor.Query -> {
                it.type.enforceClassName(it.name, STRING)
            }
        }
    }

    apply {
        val groupedArgs: Map<Class<*>, List<Argument>> = args.groupBy { it.descriptor.javaClass }
        require((groupedArgs[Argument.Descriptor.Body::class.java]?.size ?: 0) <= 1) {
            "Duplicate @Body: ${groupedArgs[Argument.Descriptor.Body::class.java]}"
        }
        require((groupedArgs[Argument.Descriptor.Outgoing::class.java]?.size ?: 0) <= 1) {
            "Duplicate @Outgoing: ${groupedArgs[Argument.Descriptor.Outgoing::class.java]}"
        }
        if (groupedArgs.containsKey(Argument.Descriptor.Body::class.java) || groupedArgs.containsKey(Argument.Descriptor.Outgoing::class.java)) {
            require(!groupedArgs.containsKey(Argument.Descriptor.Field::class.java)) {
                "@Field should not use with @Body or @Outgoing"
            }
        }
        if (groupedArgs.containsKey(Argument.Descriptor.Body::class.java) && groupedArgs.containsKey(Argument.Descriptor.Outgoing::class.java)) {
            require(!groupedArgs.containsKey(Argument.Descriptor.Field::class.java)) {
                "Duplicate @Body and @Outgoing"
            }
        }
        if (groupedArgs.containsKey(Argument.Descriptor.Body::class.java) || groupedArgs.containsKey(Argument.Descriptor.Field::class.java)) {
            require(method != Request.Method.GET) {
                "Http method GET should not contains Body"
            }
        }
    }

    val placeholders = args.filter { it.descriptor is Argument.Descriptor.Path }
        .associate { (it.descriptor as Argument.Descriptor.Path).key to it.name }

    return Request(
        name,
        modifiers,
        args,
        returning,
        method,
        path.parsePath(placeholders),
    )
}