package com.github.kr328.krestful

import com.github.kr328.krestful.client.buildOutgoing
import com.github.kr328.krestful.client.buildRequest
import com.github.kr328.krestful.client.buildResult
import com.github.kr328.krestful.model.Call
import com.github.kr328.krestful.model.Members
import com.github.kr328.krestful.model.Types
import com.github.kr328.krestful.util.*
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName

object Client {
    fun generateClientClass(
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
                val utilName = if (it.isWebSocket) Members.webSocket else Members.request

                controlFlow("return %M(baseUrl, context)", utilName) {
                    controlFlow("request") {
                        buildRequest(it)
                    }

                    if (it.isWebSocket) {
                        controlFlow("outgoing") {
                            buildOutgoing(it)
                        }
                    }
                }

                val mapping = if (it.isWebSocket) Members.map else Members.let

                controlFlow(".%M", mapping) {
                    buildResult(it)
                }
            }

            clazz.addFunction(func.build())
        }

        val factory = FunSpec.builder("create${original.simpleName.asString()}Proxy")
            .addGenerated()
            .receiver(Types.HttpClient)
            .returns(original.toClassName())
            .addParameter("baseUrl", Types.Url)
            .addParameter("json", Types.Json, "%T", Types.Json)
            .addParameter("context", Types.CoroutineContext, "%T.Default", Types.Dispatchers)
            .addCode {
                addStatement("return %L", clazz.build())
            }

        return FileSpec.builder(original.packageName.asString(), original.simpleName.asString())
            .addSuppress(
                "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
                "RedundantVisibilityModifier",
                "LocalVariableName"
            )
            .addFunction(factory.build())
            .build()
    }
}