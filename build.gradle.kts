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
    repositories {
        mavenCentral()
    }
}