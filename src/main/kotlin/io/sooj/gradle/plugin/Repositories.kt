package io.sooj.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler

// Providing most repositories so that a beginner doesn't
// have to worry about missing repositories
fun RepositoryHandler.all(project: Project) {
    mavenCentral()
    jcenter()
    mavenLocal()
    maven {
        url = project.uri("https://plugins.gradle.org/m2/")
    }
    maven {
        url = project.uri("https://dl.bintray.com/kotlin/ktor")
    }
    maven {
        url = project.uri("https://dl.bintray.com/kotlin/kotlinx")
    }
    maven {
        url = project.uri("https://jitpack.io")
    }
    maven {
        url = project.uri("https://dl.bintray.com/sooj/sooj")
    }
}