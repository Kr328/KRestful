@file:Suppress("UnstableApiUsage")

rootProject.name = "krestful"

include("runtime")
include("example")
include("processor")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }
    versionCatalogs {
        create("libs") {
            val kotlin = "1.6.20"
            val ksp = "$kotlin-1.0.5"
            val serialization = "1.3.2"
            val coroutine = "1.6.1"
            val ktor = "2.0.0"
            val autoservice = "1.0.1"
            val ktpoet = "1.11.0"

            library("kotlinx-serialization-core", "org.jetbrains.kotlinx:kotlinx-serialization-core:$serialization")
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization")
            library("kotlinx-coroutine", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutine")
            library("ktor-client-core", "io.ktor:ktor-client-core:$ktor")
            library("ktor-client-okhttp", "io.ktor:ktor-client-okhttp:$ktor")
            library("ktor-client-websockets", "io.ktor:ktor-client-websockets:$ktor")
            library("ktor-server-core", "io.ktor:ktor-server-core:$ktor")
            library("ktor-server-netty", "io.ktor:ktor-server-netty:$ktor")
            library("ktor-server-websockets", "io.ktor:ktor-server-websockets:$ktor")
            library("autoservice-processor", "com.google.auto.service:auto-service:$autoservice")
            library("autoservice-annotations", "com.google.auto.service:auto-service-annotations:$autoservice")
            library("ksp-api", "com.google.devtools.ksp:symbol-processing-api:$ksp")
            library("kotlinpoet-core", "com.squareup:kotlinpoet:$ktpoet")
            library("kotlinpoet-ksp", "com.squareup:kotlinpoet-ksp:$ktpoet")
        }
    }
}