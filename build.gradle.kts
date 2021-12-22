buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(deps.build.kotlin.lang)
        classpath(deps.build.kotlin.serialization)
        classpath(deps.build.ksp)
    }
}

allprojects {
    group = "com.github.kr328.krestful"
    version = "1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    val publish = setOf("processor", "runtime")

    if (name in publish) {
        println("- Add publishing to module '${name}'")

        apply(plugin = "maven-publish")

        afterEvaluate {
            val artifactId = name

            val sourcesJar = tasks.register("sourcesJar", type = Jar::class) {
                archiveClassifier.set("sources")
                from(project.extensions.getByType(SourceSetContainer::class.java).named("main").get().allSource)
            }

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
                    create("maven", type = MavenPublication::class) {
                        this.artifactId = artifactId
                        this.version = project.version.toString()

                        from(components["java"])

                        artifact(sourcesJar)

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
    }
}