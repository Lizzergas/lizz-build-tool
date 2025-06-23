package com.skommy.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.terminal.prompt
import com.skommy.BuildConstants
import com.skommy.CompilerConstants
import com.skommy.getCurrentFolderName
import com.skommy.gradle.GradleBuild
import com.skommy.yaml.YamlManager
import com.skommy.yaml.buildSettings
import java.io.File

class Init : CliktCommand() {
    override fun run() {
        if (YamlManager.exists()) {
            echo("lizz.yaml already exists", err = true)
            currentContext.exitProcess(1)
        }

        val name = terminal.prompt(
            prompt = "Name:",
            default = getCurrentFolderName(),
            showDefault = true
        )

        val version = terminal.prompt(
            prompt = "Version:",
            default = BuildConstants.PROJECT_VERSION,
            showDefault = true
        )

        val description = terminal.prompt(
            prompt = "Description",
            default = "",
            showDefault = false
        )

        val author = terminal.prompt(
            prompt = "Author:",
            default = System.getProperty("user.name"),
            showDefault = true
        )

        val settings = buildSettings(
            name = name.orEmpty(),
            version = version.orEmpty(),
            description = description.orEmpty(),
            author = author.orEmpty(),
            dependencies = listOf("guava", "gson", "okio"),
        )
        YamlManager.save(settings)

        val helloWorld = """
            fun main() {
                println("Hello world!")
            }
        """.trimIndent()
        val mainKt = File("main.kt")
        mainKt.writeText(helloWorld)
        GradleBuild.stubGradleSetup(CompilerConstants.ktHome.orEmpty())
        GradleBuild.syncGradleStub(listOf())
    }
}