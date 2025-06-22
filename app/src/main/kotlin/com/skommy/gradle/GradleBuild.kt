package com.skommy.gradle

import java.io.File
import java.nio.file.Files

object GradleBuild {
    val STUB = """
        // Temporary solution to support Auto-complete in Intellij before BSP support is added
        // build.gradle.kts  (generated — do not edit)
        plugins {
            id("org.jetbrains.kotlin.jvm") version "2.1.21"
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
        %DEPENDENCY_LINES%
        }

        // 4️⃣  Convenience task so devs can hit the green hammer ▶
        tasks.register<Exec>("lizz-build") {
            group = "lizz-build-tool"
            description = "Compile Kotlin JVM and output a runnable JAR file"
            commandLine("./app", "build")      // delegates to your tool
        }    
        
        tasks.register<Exec>("lizz-run") {
            group = "lizz-build-tool"
            description = "Compile Kotlin JVM and output a runnable JAR file"
            commandLine("java", "-jar", "lizz.jar")      // delegates to your tool
        }    
    """.trimIndent()

    fun stubGradleSetup(kotlinHome: String) {
        val settingsFile = File("settings.gradle.kts")
        val buildFile = File("build.gradle.kts")

        settingsFile.writeText("""rootProject.name = "lizz-project"""".trimIndent())

        val template = STUB
        buildFile.writeText(template.replace("%KOTLIN_HOME%", kotlinHome))
        println("Generated settings.gradle.kts and build.gradle.kts")
    }

    fun syncGradleStub(resolvedJars: List<File>) {
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

            buildFile.writeText(content.replace("%DEPENDENCY_LINES%", depsBlock))
        }
        println("Synced and resolved dependencies successfully!")
    }
}