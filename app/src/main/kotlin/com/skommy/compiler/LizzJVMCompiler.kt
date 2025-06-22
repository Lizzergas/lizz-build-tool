package com.skommy.compiler

import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.jar.*

class LizzJVMCompiler {

    private val ktHome = System.getenv("KOTLIN_HOME")
    private val stdLib = "$ktHome/lib/kotlin-stdlib.jar"

    fun compileKotlin(
        classPath: String
    ): ExitCode {
        println("Compiling Main.kt")

        val compiler = K2JVMCompiler()
        val args = compiler.createArguments().apply {
            destination = "lizz.jar"
            classpath = stdLib
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
            mainAttributes[Attributes.Name.EXTENSION_NAME] = "Lizz Build Tool 0.1"
            mainAttributes.putValue("Built-By", System.getProperty("user.name"))
            mainAttributes.putValue("Class-Path", stdLib)
        }
        rewriteManifest(Paths.get("lizz.jar"), manifest)
        println("Updated manifest")
    }

    fun rewriteManifest(jarPath: Path, manifest: Manifest) {
//        val tmp2 = Files.createTempFile("lizz", ".jar")
        val tmp = Files.createFile(Path.of("lizz-temp.jar"))

        JarFile(jarPath.toFile()).use { src ->
            JarOutputStream(Files.newOutputStream(tmp), manifest).use { out ->
                // copy every entry *except* the old manifest
                val buffer = ByteArray(16 * 1024)
                src.entries().asSequence()
                    .filterNot { it.name == JarFile.MANIFEST_NAME }
                    .forEach { entry ->
                        out.putNextEntry(JarEntry(entry.name).apply {
                            time = entry.time
                            size = entry.size
                            crc = entry.crc
                            method = entry.method
                        })
                        src.getInputStream(entry).use { inp ->
                            var n: Int
                            while (inp.read(buffer).also { n = it } != -1) out.write(buffer, 0, n)
                        }
                        out.closeEntry()
                    }
            }
        }
        Files.move(tmp, jarPath, StandardCopyOption.REPLACE_EXISTING)
    }
}