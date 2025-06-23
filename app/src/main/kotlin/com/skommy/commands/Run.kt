package com.skommy.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.skommy.CompilerConstants
import com.skommy.yaml.YamlManager
import java.io.IOException

class Run : CliktCommand() {
    override fun run() {
        try {
            val settings = YamlManager.load()
            val jarPath = "${CompilerConstants.buildFolder}/${settings.project.name}.jar"
            val process = ProcessBuilder("java", "-jar", jarPath).inheritIO().start()
            val exitCode = process.waitFor()
            echo("Process exited with code: $exitCode")
        } catch (e: IOException) {
            echo("Error running jar: ${e.message}")
        } catch (e: InterruptedException) {
            echo("Process was interrupted: ${e.message}")
            Thread.currentThread().interrupt()
        }
    }
}