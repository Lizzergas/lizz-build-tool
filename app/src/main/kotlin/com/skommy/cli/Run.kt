package com.skommy.cli

import com.skommy.CompilerConstants
import com.skommy.services.YamlService
import java.io.IOException

/**
 * Run command - executes the compiled JAR file.
 * Uses LizzCommand base class for automatic project root detection.
 */
class Run : LizzCommand() {
    override fun runCommand() {
        try {
            // Load settings from the located YAML file (root is guaranteed to be initialized)
            val settings = YamlService.loadFromFile(lizzYamlFile())

            // Build absolute path to the compiled jar using helper method
            val jarFile = rootFile("${CompilerConstants.buildFolder}/${settings.project.name}.jar")
            
            if (!jarFile.exists()) {
                echo("✗ JAR file not found: ${jarFile.absolutePath}", err = true)
                echo("Please run 'lizz build' first to compile the project.", err = true)
                return
            }

            // Run the jar and stream output
            echo("Running ${jarFile.absolutePath} ...")
            val exitCode = ProcessBuilder("java", "-jar", jarFile.absolutePath)
                .directory(root)  // ensure relative paths work from project root
                .inheritIO()
                .start()
                .waitFor()

            echo("Process exited with code: $exitCode")
        } catch (e: IOException) {
            echo("✗ Error running jar: ${e.message}", err = true)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            echo("✗ Process was interrupted: ${e.message}", err = true)
        }
    }
}
