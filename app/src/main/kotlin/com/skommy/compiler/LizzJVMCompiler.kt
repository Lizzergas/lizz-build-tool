package com.skommy.compiler

import com.skommy.Constants
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class LizzJVMCompiler {

    fun compileKotlin(): ExitCode {
        println("Compiling Main.kt")

        val compiler = K2JVMCompiler()
        val args = compiler.createArguments().apply {
            destination = "lizz.jar"
            classpath = Constants.stdLib
            freeArgs = listOf("main.kt")
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
        val manifest = Manifest().apply {
            mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
            mainAttributes[Attributes.Name.MAIN_CLASS] = "MainKt"
            mainAttributes[Attributes.Name.EXTENSION_NAME] = "Lizz Buildd Tool 0.1"
            mainAttributes[Attributes.Name.CLASS_PATH] = Constants.stdLib
            mainAttributes.putValue("Built-By", Constants.currentUser)
        }
        appendManifestAttrs(Paths.get("lizz.jar"), manifest)
        println("Updated manifest")
    }

    fun appendManifestAttrs(jarPath: Path, additions: Manifest) {
        JarFile(jarPath.toFile()).use { jar ->
            val tmp = Files.createTempFile("lizz-add-manifest-temp-", ".jar")
            val newManifest = jar.manifest.apply {
                for ((k, v) in additions.mainAttributes) {
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

            Files.move(tmp, jarPath, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}