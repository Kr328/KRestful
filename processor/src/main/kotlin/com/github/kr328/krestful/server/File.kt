package com.github.kr328.krestful.server

import com.github.kr328.krestful.common.Members
import com.github.kr328.krestful.common.Types
import com.github.kr328.krestful.common.gettingCode
import com.github.kr328.krestful.common.mappingCode
import com.github.kr328.krestful.model.Argument
import com.github.kr328.krestful.model.Request
import com.github.kr328.krestful.util.*
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName

fun KSClassDeclaration.generateServerFile(basePath: String, requests: List<Request>): FileSpec {
    val thisName = toClassName()

    val factory = FunSpec.builder("with${thisName.simpleName}Delegate")
        .addGenerated()
        .receiver(Types.Route)
        .returns(Types.Route)
        .addParameter("delegate", thisName)
        .addParameter("json", Types.Json, "%T", Types.Json)

    factory.addCode {
        controlFlow("%M(%S)", Members.route, basePath) {
            for (request in requests) {
                val (format, args) = if (request.method == Request.Method.WebSocket) {
                    val format = """
                        %M(
                          path = %L,
                          result = %L,
                        )
                    """.trimIndent()
                    val args = arrayOf(
                        Members.withWebSocket,
                        request.path.urlCode,
                        request.returning.typeArguments[0].mappingCode
                    )

                    format to args
                } else {
                    val format = """
                        %M(
                          method = %L,
                          path = %L,
                          result = %L,
                        )
                    """.trimIndent()
                    val args = arrayOf(
                        Members.withRequest,
                        request.method.gettingCode,
                        request.path.urlCode,
                        request.returning.mappingCode
                    )

                    format to args
                }

                controlFlow(format, *args) {
                    request.arguments.forEach {
                        when (it.descriptor) {
                            Argument.Descriptor.Body -> {
                                addBody(it.name, it.type)
                            }
                            Argument.Descriptor.Outgoing -> {
                                addOutgoing(it.name, it.type)
                            }
                            is Argument.Descriptor.Field -> {
                                addField(it.name, it.type, it.descriptor.key)
                            }
                            is Argument.Descriptor.Header -> {
                                addHeader(it.name, it.type, it.descriptor.key)
                            }
                            is Argument.Descriptor.Query -> {
                                addQuery(it.name, it.type, it.descriptor.key)
                            }
                            is Argument.Descriptor.Path -> {
                                addPath(it.name, it.descriptor.key)
                            }
                        }
                    }

                    addStatement(
                        "delegate.%N(${request.arguments.indices.joinToString(",") { "%N" }})",
                        request.name,
                        *request.arguments.map { it.name }.toTypedArray()
                    )
                }
            }
        }

        addStatement("return this")
    }

    return FileSpec.builder(thisName.packageName, "${thisName.simpleName}Delegate")
        .addSuppress(
            "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
            "RedundantVisibilityModifier",
            "LocalVariableName"
        )
        .addFunction(factory.build())
        .build()
}