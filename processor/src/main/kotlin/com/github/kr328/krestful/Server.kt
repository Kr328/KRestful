package com.github.kr328.krestful

import com.github.kr328.krestful.common.mappingBlock
import com.github.kr328.krestful.common.methodBlock
import com.github.kr328.krestful.model.*
import com.github.kr328.krestful.server.*
import com.github.kr328.krestful.util.*
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ksp.toClassName

object Server {
    fun generateServerFile(
        original: KSClassDeclaration,
        calls: List<Call>,
    ): FileSpec {
        val func = FunSpec.builder("with${original.simpleName.asString()}Delegate")
            .addGenerated()
            .receiver(Types.Route)
            .returns(Types.Route)
            .addParameter("implementation", original.toClassName())
            .addParameter("json", Types.Json, "%T", Types.Json)

        func.addCode {
            for (call in calls) {
                val path = call.path.segments.joinToString("") {
                    when (it) {
                        is UrlTemplate.Segment.Literal -> it.value
                        is UrlTemplate.Segment.Variable -> "{${it.key}}"
                    }
                }

                val (format, args) = if (call.method == HttpMethod.WebSocket) {
                    val format = """
                        %M(
                          json = json,
                          path = %S,
                          result = %L,
                        )
                    """.trimIndent()
                    val args = arrayOf(
                        Members.withWebSocket,
                        path,
                        (call.returning as ParameterizedTypeName).typeArguments[0].mappingBlock()
                    )

                    format to args
                } else {
                    val format = """
                        %M(
                          json = json,
                          method = %L,
                          path = %S,
                          result = %L,
                        )
                    """.trimIndent()
                    val args = arrayOf(
                        Members.withRequest,
                        call.method.methodBlock(),
                        path,
                        call.returning.mappingBlock()
                    )

                    format to args
                }

                controlFlow(format, *args) {
                    call.arguments.forEach {
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

                    addStatement("implementation.%N(${call.arguments.joinToString(",") { it.name }})", call.name)
                }
            }

            addStatement("return this")
        }

        return FileSpec.builder(original.packageName.asString(), original.simpleName.asString() + "Delegate")
            .addSuppress(
                "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
                "RedundantVisibilityModifier",
                "LocalVariableName"
            )
            .addFunction(func.build())
            .build()
    }
}