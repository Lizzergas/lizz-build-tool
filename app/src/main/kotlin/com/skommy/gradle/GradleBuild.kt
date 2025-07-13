package com.skommy.gradle

import com.skommy.models.BuildSettings
import com.skommy.services.LoggerProvider
import com.skommy.services.LoggerService
import java.io.File
import java.nio.file.Files

class GradleBuild(
    private val logger: LoggerService = LoggerProvider.get()
) {
    fun stubGradleSetup(kotlinHome: String) {
        val settingsFile = File("settings.gradle.kts")
        val buildFile = File("build.gradle.kts")

        settingsFile.writeText("""rootProject.name = "lizz-project"""".trimIndent())

        val template = STUB
        buildFile.writeText(template.replace("%KOTLIN_HOME%", kotlinHome))
        logger.println("Generated settings.gradle.kts and build.gradle.kts")
    }

    fun syncGradleStub(resolvedJars: List<File>, settings: BuildSettings) {
        val buildFile = File("build.gradle.kts")

        if (buildFile.exists()) {
            val content = Files.readString(buildFile.toPath())
            val depsBlock = if (resolvedJars.isEmpty()) {
                ""
            } else {
                buildString {
                    resolvedJars.forEach { jar ->
                        appendLine("""    implementation(files("${jar.absolutePath.replace("\\", "\\\\")}"))""")
                    }
                }
            }

            println("\n\n\n IS REFLECTION ENABLED: ${settings.kotlin.reflection} \n\n\n")
            val reflectLine = if (settings.kotlin.reflection) {
                """    implementation(files("%KOTLIN_HOME%/lib/kotlin-reflect.jar"))"""
            } else {
                ""
            }

            var updatedContent = content.replace("%DEPENDENCY_LINES%", depsBlock)
            updatedContent = updatedContent.replace("%KOTLIN_VERSION%", settings.kotlin.version)
            updatedContent = updatedContent.replace("%KOTLIN_REFLECT_LINE%", reflectLine)
            buildFile.writeText(updatedContent)
        }
    }

    companion object {
        val STUB = """
        // Temporary solution to support Auto-complete in Intellij before BSP support is added
        // build.gradle.kts  (generated — do not edit)
        plugins {
            id("org.jetbrains.kotlin.jvm") version "%KOTLIN_VERSION%"
        }

        repositories { mavenCentral() }

        // 1️⃣  Tell Gradle/IDE that “.” is the only source root
        sourceSets {
            main { kotlin.srcDirs(".") }
            // optional:
            // test { kotlin.srcDirs("tests") }
        }

        // 2️⃣  Kotlin/JDK toolchain to match your compiler flags
        kotlin.jvmToolchain(21)

        // 3️⃣  Add Kotlin std-lib and every resolved jar as libraries
        dependencies {
            implementation(files("%KOTLIN_HOME%/lib/kotlin-stdlib.jar"))
        %KOTLIN_REFLECT_LINE%
        %DEPENDENCY_LINES%
        }

        // 4️⃣  Convenience task so devs can hit the green hammer ▶
        tasks.register<Exec>("lizz-build") {
            group = "lizz-build-tool"
            description = "Compile Kotlin JVM and output a runnable JAR file"
            commandLine("lizz", "build")
        }    

        tasks.register<Exec>("lizz-run") {
            group = "lizz-build-tool"
            description = "Compile Kotlin JVM and output a runnable JAR file"
            commandLine("lizz", "run")
        }    
    """.trimIndent()
    }
}
