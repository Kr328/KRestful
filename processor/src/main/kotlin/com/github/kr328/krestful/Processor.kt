package com.github.kr328.krestful

import com.github.kr328.krestful.model.Types
import com.github.kr328.krestful.util.collectRawCall
import com.github.kr328.krestful.util.getAllFunctionsExceptAny
import com.github.kr328.krestful.util.toCall
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

            val calls = try {
                clazz.getAllFunctionsExceptAny()
                    .map {
                        try {
                            it.collectRawCall().toCall(resolver)
                        } catch (e: Exception) {
                            logger.error("$e", it)

                            throw e
                        }
                    }
                    .toList()
            } catch (e: Exception) {
                logger.error("Parse interface ${clazz.simpleName.asString()}: $e", clazz)

                continue
            }

            if (generateClient) {
                try {
                    Client.generateClientFile(clazz, calls)
                        .writeTo(generator, Dependencies(false, clazz.containingFile!!))
                } catch (e: Exception) {
                    logger.error("Generate client class ${clazz.simpleName.asString()}: $e", symbol)
                }
            }

            if (generateServer) {
                try {
                    Server.generateServerFile(clazz, calls)
                        .writeTo(generator, Dependencies(false, clazz.containingFile!!))
                } catch (e: Exception) {
                    logger.error("Generate server class ${clazz.simpleName.asString()}: $e", symbol)
                }
            }
        }

        return emptyList()
    }
}