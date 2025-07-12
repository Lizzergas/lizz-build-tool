package com.skommy.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.terminal.prompt
import com.skommy.BuildConstants
import com.skommy.CompilerConstants
import com.skommy.getCurrentFolderName
import com.skommy.gradle.GradleBuild
import com.skommy.models.buildSettings
import com.skommy.services.BuildService
import java.io.File

class Init : CliktCommand() {
    override fun help(context: Context): String = "Prompt user details about the project and output skeleton project"
    override fun run() {
        val buildService = BuildService()
        val gradleBuild = GradleBuild()
        if (buildService.exists()) {
            echo("${BuildConstants.CONFIG_FILE} already exists", err = true)
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
            dependencies = listOf("com.google.code.gson:gson:2.10.1"),
        )
        buildService.save(settings)

        val helloWorld = """
            import com.google.gson.Gson

            data class Person(val name: String, val age: Int)

            fun main() {
                println("Hello world!")

                    // Test Gson
                    val gson = Gson()
                    val person = Person("John Doe", 30)
                    val json = gson.toJson(person)
                    println(json)
            }
        """.trimIndent()
        val mainKt = File(BuildConstants.MAIN_KT)
        mainKt.writeText(helloWorld)
        gradleBuild.stubGradleSetup(CompilerConstants.getKotlinHome())
        gradleBuild.syncGradleStub(listOf(), settings.kotlin.version)

        val gitIgnore = File(".gitignore")
        gitIgnore.writeText(
            """
            settings.gradle.kts
            build.gradle.kts
            build/
        """.trimIndent()
        )
    }
}
