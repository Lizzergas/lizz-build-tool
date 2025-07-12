package com.skommy.cli

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.skommy.models.Script
import com.skommy.services.BuildService

class Sh : LizzCommand() {
    private val name: String? by argument().optional()

    override fun runCommand() {
        val buildService = BuildService()
        val settings = buildService.load(yamlFile())
        val scripts = settings.scripts

        if (name == null) {
            // Print all available scripts
            printAvailableScripts(scripts)
        } else {
            // Execute the specified script
            executeScript(name!!, scripts)
        }
    }

    private fun printAvailableScripts(scripts: Map<String, Script>) {
        if (scripts.isEmpty()) {
            echo("No scripts available.")
            return
        }

        echo("Available scripts:")
        scripts.forEach { (key, script) ->
            when (script) {
                is Script.SimpleScript -> {
                    echo("  $key: ${script.command}")
                }
            }
        }
    }

    private fun executeScript(scriptName: String, scripts: Map<String, Script>) {
        val script = scripts[scriptName]
        if (script == null) {
            echo("Script '$scriptName' not found.")
            echo("Available scripts: ${scripts.keys.joinToString(", ")}")
            return
        }

        val command = script.command
        echo("Executing script '$scriptName': $command")

        try {
            val process = ProcessBuilder("sh", "-c", command)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                echo("Script '$scriptName' completed successfully.")
            } else {
                echo("Script '$scriptName' exited with code $exitCode")
            }
        } catch (e: Exception) {
            echo("Error executing script '$scriptName': ${e.message}")
            e.printStackTrace()
        }
    }
}
