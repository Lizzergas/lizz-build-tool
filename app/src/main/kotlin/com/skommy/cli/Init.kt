package com.skommy.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.terminal.prompt
import com.skommy.BuildConstants
import com.skommy.CompilerConstants
import com.skommy.getCurrentFolderName
import com.skommy.gradle.GradleBuild
import com.skommy.models.buildSettings
import com.skommy.services.BuildService
import java.io.File

class Init : CliktCommand() {
    private val testFolderName: String? by option("--test", help = "Create a test project with predefined values in the specified folder")

    override fun help(context: Context): String = "Prompt user details about the project and output skeleton project"
    override fun run() {
        if (testFolderName != null) {
            runTestMode(testFolderName!!)
        } else {
            runInteractiveMode()
        }
    }

    private fun runTestMode(folderName: String) {
        val buildService = BuildService()
        val gradleBuild = GradleBuild()

        // Check if lizz.yaml already exists in current directory
        if (buildService.yamlExists()) {
            echo("${BuildConstants.CONFIG_FILE} already exists", err = true)
            currentContext.exitProcess(1)
        }

        // Use predefined test values (no prompts)
        val settings = buildSettings(
            name = folderName,
            version = "1.0.0-test",
            description = "Test project for AI testing",
            author = "lizz-test",
            dependencies = listOf("com.google.code.gson:gson:2.10.1"),
        )
        buildService.save(settings)

        createProjectFiles(gradleBuild, settings, File("."))

        echo("✓ Test project '$folderName' created successfully")
        echo("✓ Project files created in current directory")
    }

    private fun runInteractiveMode() {
        val buildService = BuildService()
        val gradleBuild = GradleBuild()

        if (buildService.yamlExists()) {
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

        createProjectFiles(gradleBuild, settings, File("."))
    }

    private fun createProjectFiles(gradleBuild: GradleBuild, settings: com.skommy.models.BuildSettings, targetDir: File) {
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

        // Create Main.kt in target directory
        val mainKt = File(targetDir, BuildConstants.MAIN_KT)
        mainKt.writeText(helloWorld)

        // Change to target directory for Gradle operations
        val originalDir = System.getProperty("user.dir")
        System.setProperty("user.dir", targetDir.absolutePath)

        try {
            gradleBuild.stubGradleSetup(CompilerConstants.getKotlinHome())
            gradleBuild.syncGradleStub(listOf(), settings)
        } finally {
            System.setProperty("user.dir", originalDir)
        }

        // Create .gitignore in target directory
        val gitIgnore = File(targetDir, ".gitignore")
        gitIgnore.writeText(
            """
            settings.gradle.kts
            build.gradle.kts
            build/
        """.trimIndent()
        )
    }
}
