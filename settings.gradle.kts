@file:Suppress("UnstableApiUsage")

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "krestful"

include("runtime")
include("example")
include("processor")

dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            val kotlin = "1.6.0"
            val serialization = "1.3.1"
            val coroutine = "1.5.2"
            val ktor = "1.6.7"
            val autoservice = "1.0.1"
            val ksp = "1.6.0-1.0.2"
            val ktpoet = "1.10.2"

            alias("build-kotlin-lang").to("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin")
            alias("build-kotlin-serialization").to("org.jetbrains.kotlin:kotlin-serialization:$kotlin")
            alias("build-ksp").to("com.google.devtools.ksp:symbol-processing-gradle-plugin:$ksp")
            alias("kotlinx-serialization-core").to("org.jetbrains.kotlinx:kotlinx-serialization-core:$serialization")
            alias("kotlinx-serialization-json").to("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization")
            alias("kotlinx-coroutine").to("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutine")
            alias("ktor-client-core").to("io.ktor:ktor-client-core:$ktor")
            alias("ktor-client-okhttp").to("io.ktor:ktor-client-okhttp:$ktor")
            alias("ktor-client-websockets").to("io.ktor:ktor-client-websockets:$ktor")
            alias("ktor-server-core").to("io.ktor:ktor-server-core:$ktor")
            alias("ktor-server-netty").to("io.ktor:ktor-server-netty:$ktor")
            alias("ktor-server-websockets").to("io.ktor:ktor-websockets:$ktor")
            alias("autoservice-processor").to("com.google.auto.service:auto-service:$autoservice")
            alias("autoservice-annotations").to("com.google.auto.service:auto-service-annotations:$autoservice")
            alias("ksp-api").to("com.google.devtools.ksp:symbol-processing-api:$ksp")
            alias("kotlinpoet-core").to("com.squareup:kotlinpoet:$ktpoet")
            alias("kotlinpoet-ksp").to("com.squareup:kotlinpoet-ksp:$ktpoet")
        }
    }
}