plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

dependencies {
    compileOnly(libs.ktor.client.core)
    compileOnly(libs.ktor.client.websockets)
    compileOnly(libs.ktor.server.core)
    compileOnly(libs.ktor.server.websockets)
    compileOnly(libs.kotlinx.serialization.core)
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.coroutine)
}

publishing {
    publications {
        create(project.name, MavenPublication::class) {
            from(components["java"])

            artifact(tasks["sourcesJar"])
        }
    }
}
