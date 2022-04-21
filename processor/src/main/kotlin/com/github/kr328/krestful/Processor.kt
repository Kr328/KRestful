package com.github.kr328.krestful

import com.github.kr328.krestful.client.generateClientFile
import com.github.kr328.krestful.common.Types
import com.github.kr328.krestful.common.collectRequest
import com.github.kr328.krestful.common.refine
import com.github.kr328.krestful.server.generateServerFile
import com.github.kr328.krestful.util.getAllFunctionsExceptAny
import com.github.kr328.krestful.util.toAnnotation
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.writeTo

class Processor(
    private val generator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val generateClient = (resolver.getClassDeclarationByName(Types.HttpClient.reflectionName()) != null)
        val generateServer = (resolver.getClassDeclarationByName(Types.HttpMethod.reflectionName()) != null)

        if (!generateClient && !generateServer) {
            return emptyList()
        }

        for (symbol in resolver.getSymbolsWithAnnotation(Types.Restful.reflectionName(), true)) {
            val clazz = symbol as? KSClassDeclaration

            if (clazz == null || clazz.classKind != ClassKind.INTERFACE) {
                logger.error("@Restful only support interface", symbol)

                continue
            }

            val basePath = clazz.annotations
                .map { it.toAnnotation() }
                .single { it.type == Types.Restful }
                .values["path"] as? String ?: ""

            val calls = try {
                clazz.getAllFunctionsExceptAny()
                    .map {
                        try {
                            it.collectRequest().refine(resolver)
                        } catch (e: Exception) {
                            logger.error("${e.message}", it)

                            throw e
                        }
                    }
                    .toList()
            } catch (e: Exception) {
                continue
            }

            if (generateClient) {
                try {
                    clazz.generateClientFile(basePath, calls)
                        .writeTo(generator, Dependencies(false, clazz.containingFile!!))
                } catch (e: Exception) {
                    logger.error("Generate client class ${clazz.simpleName.asString()}: $e", symbol)
                }
            }

            if (generateServer) {
                try {
                    clazz.generateServerFile(basePath, calls)
                        .writeTo(generator, Dependencies(false, clazz.containingFile!!))
                } catch (e: Exception) {
                    logger.error("Generate server class ${clazz.simpleName.asString()}: $e", symbol)
                }
            }
        }

        return emptyList()
    }
}