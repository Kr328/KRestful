package com.github.kr328.krestful.client

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
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName

fun KSClassDeclaration.generateClientFile(requests: List<Request>): FileSpec {
    val thisName = this.toClassName()

    val clazz = TypeSpec.anonymousClassBuilder()
        .addSuperinterface(thisName)

    for (request in requests) {
        val func = FunSpec.builder(request.name)
            .addModifiers(request.modifiers + KModifier.OVERRIDE - KModifier.ABSTRACT)
            .returns(request.returning)

        request.arguments.forEach {
            func.addParameter(it.name, it.type)
        }

        func.addCode {
            val (format: String, args: Array<Any>) = if (request.method == Request.Method.WebSocket) {
                val format = """
                        return %M(
                          context = context,
                          json = json,
                          url = baseUrl,
                          path = %L,
                          returning = %L,
                        )
                    """.trimIndent()
                val args = arrayOf(
                    Members.webSocket,
                    request.path.templateCode,
                    request.returning.typeArguments[0].mappingCode
                )

                format to args
            } else {
                val format = """
                        return %M(
                          context = context,
                          json = json,
                          url = baseUrl,
                          path = %L,
                          method = %L,
                          returning = %L,
                        )
                    """.trimIndent()
                val args = arrayOf(
                    Members.request,
                    request.path.templateCode,
                    request.method.gettingCode,
                    request.returning.mappingCode
                )

                format to args
            }

            controlFlow(format, *args) {
                request.arguments.forEach {
                    when (it.descriptor) {
                        Argument.Descriptor.Body ->
                            addBody(it.name, it.type)
                        Argument.Descriptor.Outgoing ->
                            addOutgoing(it.name, it.type)
                        is Argument.Descriptor.Field ->
                            addField(it.name, it.type, it.descriptor.key)
                        is Argument.Descriptor.Header ->
                            addHeader(it.name, it.descriptor.key)
                        is Argument.Descriptor.Query ->
                            addQuery(it.name, it.descriptor.key)
                        is Argument.Descriptor.Path -> Unit
                    }
                }
            }
        }

        clazz.addFunction(func.build())
    }

    val factory = FunSpec.builder("create${simpleName.asString()}Proxy")
        .addGenerated()
        .receiver(Types.HttpClient)
        .returns(thisName)
        .addParameter("baseUrl", Types.Url)
        .addParameter("json", Types.Json, "%T", Types.Json)
        .addParameter("context", Types.CoroutineContext, "%T.Default", Types.Dispatchers)
        .addCode {
            addStatement("return %L", clazz.build())
        }

    return FileSpec.builder(thisName.packageName, "${thisName.simpleName}Proxy")
        .addSuppress(
            "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
            "RedundantVisibilityModifier",
            "LocalVariableName"
        )
        .addFunction(factory.build())
        .build()
}