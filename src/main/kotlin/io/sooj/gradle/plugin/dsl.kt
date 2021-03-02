package io.sooj.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

fun SoojPluginExtension.common(configure: SoojSource.() -> Unit) {
    browser.apply(configure)
}
fun SoojPluginExtension.browser(configure: SoojSource.() -> Unit) {
    browser.apply(configure)
}
fun SoojPluginExtension.server(configure: SoojSource.() -> Unit) {
    server.apply(configure)
}

fun SoojSource.dependencies(configure: KotlinDependencyHandler.() -> Unit) {
    dependency = {
        configure()
    }
}

fun Project.sooj(configure: SoojPluginExtension.() -> Unit) {
    (this as ExtensionAware).extensions.configure(
        "sooj",
        configure
    )
    plugins.getAt(SoojPlugin::class.java).extensionInitialized(
        this,
        extensions.getByType()
    )
}