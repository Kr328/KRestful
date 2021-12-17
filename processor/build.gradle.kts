import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview"
}

dependencies {
    kapt(deps.autoservice.processor)

    compileOnly(deps.autoservice.annotations)

    implementation(deps.ksp.api)
    implementation(deps.kotlinpoet.core)
    implementation(deps.kotlinpoet.ksp)
}
