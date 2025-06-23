package com.skommy.compiler

import com.skommy.CompilerConstants
import com.skommy.yaml.BuildSettings
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class LizzJVMCompiler(
    private val settings: BuildSettings
) {
    val jarPath = "${CompilerConstants.buildFolder}/${settings.project.name}.jar"

    fun compileKotlin(): ExitCode {
        println("Compiling Main.kt")

        val compiler = K2JVMCompiler()
        val args = compiler.createArguments().apply {
            destination = jarPath
            classpath = CompilerConstants.stdLib
            freeArgs = findKotlinFilesTwo()
            disableStandardScript = true
            noStdlib = true
            noReflect = true
            disableDefaultScriptingPlugin = true
        }

        val exit = compiler.exec(
            messageCollector = PrintingMessageCollector(
                System.err,
                MessageRenderer.GRADLE_STYLE,
                true,
            ), services = Services.EMPTY,
            arguments = args
        )
        println("Exit: ${exit.name} ${exit.code}")
        return exit
    }

    fun updateJarManifest() {
        val jarFile = Paths.get(jarPath)
        val manifest = Manifest().apply {
            mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
            mainAttributes[Attributes.Name.MAIN_CLASS] = settings.project.mainClass
            mainAttributes[Attributes.Name.SPECIFICATION_TITLE] = settings.project.description
            mainAttributes[Attributes.Name.SPECIFICATION_VENDOR] = CompilerConstants.currentUser
            mainAttributes[Attributes.Name.CLASS_PATH] = CompilerConstants.stdLib
        }

        JarFile(jarFile.toFile()).use { jar ->
            val tmp = Files.createTempFile("${settings.project.name}-temp-", ".jar")
            val newManifest = jar.manifest.apply {
                for ((k, v) in manifest.mainAttributes) {
                    val key = k as? Attributes.Name ?: continue
                    val value = v as? String ?: continue
                    this.mainAttributes[key] = value
                }
            }

            Files.newOutputStream(tmp).use { outStream ->
                JarOutputStream(outStream, newManifest).use { jarOut ->
                    jar.entries().asSequence()
                        .filter { it.name != JarFile.MANIFEST_NAME }
                        .forEach { entry ->
                            jarOut.putNextEntry(entry)
                            jar.getInputStream(entry).use { input ->
                                input.copyTo(jarOut)
                            }
                            jarOut.closeEntry()
                        }
                }
            }

            Files.move(tmp, jarFile, StandardCopyOption.REPLACE_EXISTING)
        }
        println("Updated manifest")
    }

    private fun findKotlinFilesTwo(): List<String> {
        val root = Paths.get(".").toAbsolutePath().normalize()
        return Files.walk(root)
            .filter { path ->
                path.toString().endsWith(".kt") &&
                        !path.startsWith(root.resolve(CompilerConstants.buildFolder)) &&
                        !path.toString().contains("gradle/wrapper") &&
                        !path.fileName.toString().endsWith(".gradle.kts") &&
                        path.fileName.toString() != "settings.gradle.kts"
            }
            .map { root.relativize(it).toString() }
            .onClose { /* Files.walk uses a stream that must be closed */ }
            .toList()
    }
}