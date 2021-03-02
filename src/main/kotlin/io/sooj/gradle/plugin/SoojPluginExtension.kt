package io.sooj.gradle.plugin

import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

open class SoojPluginExtension {
    val common = SoojSource()
    val browser = SoojSource()
    val server = SoojSource()
}

open class SoojSource {
    var dependency: (KotlinDependencyHandler.() -> Unit)? = null
}