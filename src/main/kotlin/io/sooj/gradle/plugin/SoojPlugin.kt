package io.sooj.gradle.plugin

import io.sooj.gradle.DEV_PROPERTY
import io.sooj.gradle.JVM_TARGET
import io.sooj.gradle.MAIN_CLASS_NAME
import io.sooj.gradle.SoojMeta
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin
import java.io.File

class SoojPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            extensions.create<SoojPluginExtension>("sooj")

            buildscript {
                repositories {
                    all(project)
                }
            }
            repositories {
                all(project)
            }
        }
    }

    fun extensionInitialized(project: Project, extension: SoojPluginExtension) {
        with(project) {
            plugins.apply(ApplicationPlugin::class.java)
            plugins.apply(KotlinMultiplatformPluginWrapper::class.java)
            plugins.apply(SerializationGradleSubplugin::class.java)

//            configureApplication {
//                mainClassName = "ServerKt"
//            }

            configureMultiplatform {
                jvm("server") {
                    compilations.all {
                        kotlinOptions.jvmTarget = JVM_TARGET
                    }
                    withJava()
                    val serverJar by tasks.getting(org.gradle.jvm.tasks.Jar::class) {
                        doFirst {
                            manifest {
                                attributes["Main-Class"] = MAIN_CLASS_NAME
                            }
                            from(configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) })
                        }
                    }
                }
                js("browser", IR) {
                    browser {
                        binaries.executable()

                        webpackTask {
                            cssSupport.enabled = true
                            if (mode == org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode.PRODUCTION) {
                                cssSupport.mode = org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackCssMode.EXTRACT
                            }
                        }
                        runTask {
                            cssSupport.enabled = true
                            if (mode == org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode.PRODUCTION) {
                                cssSupport.mode = org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackCssMode.EXTRACT
                            }
                        }
                        testTask {
                            useKarma {
                                useChromeHeadless()
                                webpackConfig.cssSupport.enabled = true
                            }
                        }
                    }

                    compilations["main"].packageJson {
                        customField("browserslist", arrayOf("last 2 versions"))
                    }

//                    val main by compilations.getting {
//                        packageJson {
//                            customField("browserslist", arrayOf("last 2 versions"))
////                                devDependencies += arrayOf(
////                                        "css-loader" to "3.2.0",
////                                        "mini-css-extract-plugin" to "0.8.0",
////                                )
//                        }
//                    }
//                    nodejs {
//                    }
                }

                sourceSets {
                    val commonMain by getting {
                        dependencies {
                            implementation("io.sooj:sooj:$SOOJ_VERSION")
                            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
                            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$SERIALIZATION_VERSION")
                            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$SERIALIZATION_VERSION")
                            implementation("org.jetbrains.kotlinx:kotlinx-datetime:$DATE_TIME_VERSION")
                            extension.common.dependency?.invoke(this)
                        }
                    }
                    val browserMain by getting {
                        dependencies {
                            implementation(devNpm("postcss-loader", "4.1.0"))
                            implementation(devNpm("postcss", "8.1.10"))
                            implementation(devNpm("raw-loader", ""))
                            implementation(npm("tailwindcss", "v2.0.1"))
                            extension.browser.dependency?.invoke(this)
                        }
                    }
                    val serverMain by getting {
                        dependencies {
                            implementation("io.ktor:ktor-server-netty:$KTOR_VERSION")
                            implementation("io.ktor:ktor-serialization:$KTOR_VERSION")

                            implementation("io.ktor:ktor-client-core:$KTOR_VERSION")
                            implementation("io.ktor:ktor-client-json:$KTOR_VERSION")
                            implementation("io.ktor:ktor-client-serialization:$KTOR_VERSION")
                            extension.server.dependency?.invoke(this)
                        }
                    }

                    val commonTest by getting {
                        dependencies {
                            implementation(kotlin("test-common"))
                            implementation(kotlin("test-annotations-common"))
                        }
                    }
                    val serverTest by getting {
                        dependencies {
                            implementation(kotlin("test-junit"))
                        }
                    }
                    val browserTest by getting {
                        dependencies {
                            implementation(kotlin("test-js"))
                        }
                    }
                }
            }

            configureTasks()
        }
    }

    private fun Project.configureTasks() {
        val generateSoojMeta = tasks.register("generateSoojMeta", SoojMeta::class.java).get()
        val serverJar = tasks.getByName<Jar>("serverJar")

        val browserBrowserProductionWebpack = tasks.getByName<KotlinWebpack>("browserBrowserProductionWebpack") {
            outputFileName = "output.js"
        }

        tasks.create<JavaExec>("serverRunDev").apply {
            systemProperty(DEV_PROPERTY, true)

            dependsOn(serverJar)
            classpath(serverJar)
        }

        tasks.getByName<Jar>("serverJar") {
            dependsOn(generateSoojMeta)

            from(File(browserBrowserProductionWebpack.destinationDirectory, browserBrowserProductionWebpack.outputFileName)) {
                rename {
                    "${generateSoojMeta.uuid}.js"
                }
                into("public")
            }
            from(File(browserBrowserProductionWebpack.destinationDirectory, "main.css")) {
                rename {
                    "${generateSoojMeta.uuid}.css"
                }
                into("public")
            }
            from(File(generateSoojMeta.filePath, generateSoojMeta.fileName))
        }

        tasks.getByName<JavaExec>("run").apply {
            dependsOn(browserBrowserProductionWebpack)
            dependsOn(serverJar)
            classpath(serverJar)
        }
    }

    fun Project.configureMultiplatform(configure: KotlinMultiplatformExtension.() -> Unit) {
        (this as ExtensionAware).extensions.configure(
            "kotlin",
            configure
        )
    }

    fun Project.configureApplication(configure: JavaApplication.() -> Unit): Unit {
        (this as ExtensionAware).extensions.configure("application", configure)
    }
}