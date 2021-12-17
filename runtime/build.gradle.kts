plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(deps.ktor.client)
    compileOnly(deps.ktor.server)
    compileOnly(deps.kotlinx.serialization.core)
    compileOnly(deps.kotlinx.serialization.json)
    compileOnly(deps.kotlinx.coroutine)
}