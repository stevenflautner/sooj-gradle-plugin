package io.sooj.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.*

open class SoojMeta : DefaultTask() {

    val fileName = "sooj.json"
    val filePath by lazy {
        // Using buildDir instead of project.buildDir results
        // in a Error Inject() annotation required for constructor
        project.buildDir.path
    }
    val uuid: String by lazy {
        val uuid = UUID.randomUUID().toString()
        println("Sooj build id: $uuid")
        uuid
    }

    @TaskAction
    fun generateFile() {
        val file = File("$filePath/$fileName")
        if (!file.exists())
            file.createNewFile()

        file.writeText(uuid)
    }
}