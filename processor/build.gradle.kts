import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish`
}

dependencies {
    kapt(libs.autoservice.processor)

    compileOnly(libs.autoservice.annotations)

    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet.core)
    implementation(libs.kotlinpoet.ksp)
}

task("sourcesJar", Jar::class) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.getByName("main").kotlin)
}

publishing {
    publications {
        create(project.name, MavenPublication::class) {
            from(components["java"])

            artifact(tasks["sourcesJar"])
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview"
    }
}