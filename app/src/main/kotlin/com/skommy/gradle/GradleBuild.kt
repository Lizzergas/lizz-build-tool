package com.skommy.gradle

import com.skommy.models.BuildSettings
import com.skommy.services.Logger
import java.io.File
import java.nio.file.Files

class GradleBuild {
    fun stubGradleSetup(kotlinHome: String) {
        val settingsFile = File("settings.gradle.kts")
        val buildFile = File("build.gradle.kts")

        settingsFile.writeText("""rootProject.name = "lizz-project"""".trimIndent())

        val template = STUB
        buildFile.writeText(template.replace("%KOTLIN_HOME%", kotlinHome))
        Logger.println("Generated settings.gradle.kts and build.gradle.kts")
    }

    fun syncGradleStub(resolvedJars: List<File>, settings: BuildSettings) {
        val buildFile = File("build.gradle.kts")

        if (buildFile.exists()) {
            val content = Files.readString(buildFile.toPath())

            // Find the dependencies block and rebuild it
            val dependenciesStart = content.indexOf("dependencies {")
            val dependenciesEnd = content.indexOf("}", dependenciesStart) + 1

            if (dependenciesStart != -1 && dependenciesEnd != -1) {
                val beforeDependencies = content.substring(0, dependenciesStart)
                val afterDependencies = content.substring(dependenciesEnd)

                // Build new dependencies block
                val newDependenciesBlock = buildString {
                    appendLine("dependencies {")
                    appendLine("""    implementation(files("${settings.kotlin.kotlinHome}/lib/kotlin-stdlib.jar"))""")

                    // Add reflection if enabled
                    if (settings.kotlin.reflection) {
                        appendLine("""    implementation(files("${settings.kotlin.kotlinHome}/lib/kotlin-reflect.jar"))""")
                    }

                    // Add resolved dependencies (filter out kotlin-stdlib to avoid duplicates)
                    resolvedJars.filter { jar ->
                        !jar.name.startsWith("kotlin-stdlib")
                    }.forEach { jar ->
                        appendLine("""    implementation(files("${jar.absolutePath.replace("\\", "\\\\")}"))""")
                    }

                    append("}")
                }

                val updatedContent = beforeDependencies + newDependenciesBlock + afterDependencies
                buildFile.writeText(updatedContent)
            } else {
                Logger.println("Warning: Could not find dependencies block in build.gradle.kts")
            }
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
