package com.skommy.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.skommy.CompilerConstants
import com.skommy.yaml.YamlManager
import java.io.File
import java.io.IOException

class Run : CliktCommand() {
    override fun run() {
        // 1️⃣  Walk upward until we find a directory containing lizz.yaml
        val projectRoot = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
            .firstOrNull { File(it, "lizz.yaml").exists() }

        if (projectRoot == null) {
            echo("lizz.yaml not found in this directory or any parent directories.", err = true)
            return
        }

        try {
            // 2️⃣  Load settings from the located YAML file
            val settings = YamlManager.load(File(projectRoot, "lizz.yaml"))

            // 3️⃣  Build absolute path to the compiled jar
            val jarPath = File(
                projectRoot,
                "${CompilerConstants.buildFolder}/${settings.project.name}.jar"
            ).absolutePath

            // 4️⃣  Run the jar and stream output
            echo("Running $jarPath ...")
            val exitCode = ProcessBuilder("java", "-jar", jarPath)
                .directory(projectRoot)          // ensure relative paths work
                .inheritIO()
                .start()
                .waitFor()

            echo("Process exited with code: $exitCode")
        } catch (e: IOException) {
            echo("Error running jar: ${e.message}", err = true)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            echo("Process was interrupted: ${e.message}", err = true)
        }
    }
}