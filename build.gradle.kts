import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kt = "1.6.20"
    val ksp = "$kt-1.0.5"

    kotlin("jvm") version kt apply false
    kotlin("kapt") version kt apply false
    kotlin("plugin.serialization") version kt apply false
    id("com.google.devtools.ksp") version ksp apply false
}

subprojects {
    group = "com.github.kr328.krestful"
    version = "2.5"

    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
            repositories {
                mavenLocal()
                maven {
                    name = "kr328app"
                    url = uri("https://maven.kr328.app/releases")
                    credentials(PasswordCredentials::class.java)
                }
            }
            publications {
                withType(MavenPublication::class) {
                    pom {
                        name.set("KRestful")
                        description.set("A reflectless retrofit implementation for Ktor")
                        url.set("https://github.com/Kr328/KRestful")
                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("https://github.com/Kr328/KRestful/blob/main/LICENSE")
                            }
                        }
                        developers {
                            developer {
                                name.set("Kr328")
                            }
                        }
                        scm {
                            connection.set("scm:git:https://github.com/Kr328/KRestful.git")
                            url.set("https://github.com/Kr328/KRestful")
                        }
                    }
                }
            }
        }
    }
    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }
    plugins.withId("org.jetbrains.kotlin.jvm") {
        tasks.withType(KotlinCompile::class) {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
}