package com.github.kr328.krestful

import com.github.kr328.krestful.client.*
import com.github.kr328.krestful.common.mappingBlock
import com.github.kr328.krestful.common.methodBlock
import com.github.kr328.krestful.model.*
import com.github.kr328.krestful.util.*
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName

object Client {
    fun generateClientFile(
        original: KSClassDeclaration,
        calls: List<Call>
    ): FileSpec {
        val clazz = TypeSpec.anonymousClassBuilder()
            .addSuperinterface(original.toClassName())

        calls.forEach {
            val func = FunSpec.builder(it.name)
                .addModifiers(it.modifiers + KModifier.OVERRIDE - KModifier.ABSTRACT)
                .returns(it.returning)

            it.arguments.forEach { arg ->
                func.addParameter(arg.name, arg.type)
            }

            func.addCode {
                val (format: String, args: Array<Any>) = if (it.method == HttpMethod.WebSocket) {
                    val format = """
                        return %M(
                          context = context,
                          json = json,
                          url = baseUrl,
                          path = "%L",
                          returning = %L,
                        )
                    """.trimIndent()
                    val args = arrayOf(
                        Members.webSocket,
                        it.path.urlBlock(),
                        (it.returning as ParameterizedTypeName).typeArguments[0].mappingBlock()
                    )

                    format to args
                } else {
                    val format = """
                        return %M(
                          context = context,
                          json = json,
                          url = baseUrl,
                          path = "%L",
                          method = %L,
                          returning = %L,
                        )
                    """.trimIndent()
                    val args = arrayOf(
                        Members.request,
                        it.path.urlBlock(),
                        it.method.methodBlock(),
                        it.returning.mappingBlock()
                    )

                    format to args
                }

                controlFlow(format, *args) {
                    it.arguments.forEach { arg ->
                        when (arg.descriptor) {
                            Argument.Descriptor.Body ->
                                addBody(arg.name, arg.type)
                            Argument.Descriptor.Outgoing ->
                                addOutgoing(arg.name, arg.type)
                            is Argument.Descriptor.Field ->
                                addField(arg.name, arg.type, arg.descriptor.key)
                            is Argument.Descriptor.Header ->
                                addHeader(arg.name, arg.descriptor.key)
                            is Argument.Descriptor.Query ->
                                addQuery(arg.name, arg.descriptor.key)
                            is Argument.Descriptor.Path -> Unit
                        }
                    }
                }
            }

            clazz.addFunction(func.build())
        }

        val func = FunSpec.builder("create${original.simpleName.asString()}Proxy")
            .addGenerated()
            .receiver(Types.HttpClient)
            .returns(original.toClassName())
            .addParameter("baseUrl", Types.Url)
            .addParameter("json", Types.Json, "%T", Types.Json)
            .addParameter("context", Types.CoroutineContext, "%T.Default", Types.Dispatchers)
            .addCode {
                addStatement("return %L", clazz.build())
            }

        return FileSpec.builder(original.packageName.asString(), original.simpleName.asString() + "Proxy")
            .addSuppress(
                "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
                "RedundantVisibilityModifier",
                "LocalVariableName"
            )
            .addFunction(func.build())
            .build()
    }
}