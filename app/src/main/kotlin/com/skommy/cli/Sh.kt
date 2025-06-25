package com.skommy.cli

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.skommy.models.Script
import com.skommy.services.BuildSettingsService

class Sh : LizzCommand() {
    private val name: String? by argument().optional()

    override fun runCommand() {
        val settings = BuildSettingsService.load(yamlFile())
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
            println("No scripts available.")
            return
        }

        println("Available scripts:")
        scripts.forEach { (key, script) ->
            when (script) {
                is Script.SimpleScript -> {
                    println("  $key: ${script.command}")
                }
            }
        }
    }

    private fun executeScript(scriptName: String, scripts: Map<String, Script>) {
        val script = scripts[scriptName]
        if (script == null) {
            println("Script '$scriptName' not found.")
            println("Available scripts: ${scripts.keys.joinToString(", ")}")
            return
        }

        val command = script.command
        println("Executing script '$scriptName': $command")

        try {
            val process = ProcessBuilder("sh", "-c", command)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                println("Script '$scriptName' completed successfully.")
            } else {
                println("Script '$scriptName' exited with code $exitCode")
            }
        } catch (e: Exception) {
            println("Error executing script '$scriptName': ${e.message}")
            e.printStackTrace()
        }
    }
}