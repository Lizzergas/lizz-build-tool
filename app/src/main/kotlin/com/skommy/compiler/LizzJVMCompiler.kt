package com.skommy.compiler

import com.skommy.CompilerConstants
import com.skommy.models.BuildSettings
import com.skommy.services.DependencyService
import com.skommy.services.LoggerProvider
import com.skommy.services.LoggerService
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
    private val settings: BuildSettings,
    private val projectRoot: File = File(System.getProperty("user.dir")),
    private val logger: LoggerService = LoggerProvider.get()
) {
    val jarPath = File(projectRoot, "${CompilerConstants.buildFolder}/${settings.project.name}.jar").absolutePath
    private val dependencyService = DependencyService(projectRoot = projectRoot)

    private fun buildClasspath(): String {
        return dependencyService.getCompilationClasspath(settings).joinToString(File.pathSeparator)
    }

    fun compileKotlin(): ExitCode {
        logger.println("Compiling Kotlin files...")

        val classpath = buildClasspath()
        logger.println("Using classpath: $classpath")

        val compiler = K2JVMCompiler()
        val args = compiler.createArguments().apply {
            destination = jarPath
            this.classpath = classpath
            freeArgs = findKotlinFiles()
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
        logger.println("Exit: ${exit.name} ${exit.code}")
        return exit
    }

    fun updateJarManifest() {
        val jarFile = Paths.get(jarPath)

        // Build classpath for manifest (relative paths for external dependencies)
        val manifestClasspath = buildManifestClasspath()

        val manifest = Manifest().apply {
            mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
            mainAttributes[Attributes.Name.MAIN_CLASS] = settings.project.mainClass
            mainAttributes[Attributes.Name.SPECIFICATION_TITLE] = settings.project.description
            mainAttributes[Attributes.Name.SPECIFICATION_VENDOR] = CompilerConstants.currentUser
            if (manifestClasspath.isNotBlank()) {
                mainAttributes[Attributes.Name.CLASS_PATH] = manifestClasspath
            }
        }

        // Create fat JAR with all dependencies included
        createFatJar(jarFile, manifest)
        logger.println("classpath: $manifestClasspath")
        logger.println("Created fat JAR with all dependencies")
    }

    private fun buildManifestClasspath(): String {
        val classpaths = mutableListOf<String>()

        // Add Kotlin standard library (use relative path or include in fat JAR)
        val kotlinStdlib = File(CompilerConstants.getStdLib(settings))
        if (kotlinStdlib.exists()) {
            classpaths.add(kotlinStdlib.name)
        } else {
            logger.println("KOTLIN_HOME was not set properly for path: ${CompilerConstants.getStdLib(settings)}")
        }

        // For fat JAR, we don't need external classpath entries since everything is included
        // But we keep this for compatibility if someone wants to use external JARs
        val resolvedDeps = dependencyService.getCachedDependencies()
        resolvedDeps.map { File(it).name }.forEach { classpaths.add(it) }

        return classpaths.joinToString(" ")
    }

    private fun createFatJar(jarFile: java.nio.file.Path, manifest: Manifest) {
        val tmp = Files.createTempFile("${settings.project.name}-fat-", ".jar")
        val addedEntries = mutableSetOf<String>()

        Files.newOutputStream(tmp).use { outStream ->
            JarOutputStream(outStream, manifest).use { jarOut ->
                // First, add all entries from the original compiled JAR
                JarFile(jarFile.toFile()).use { originalJar ->
                    originalJar.entries().asSequence()
                        .filter { it.name != JarFile.MANIFEST_NAME }
                        .forEach { entry ->
                            if (!addedEntries.contains(entry.name)) {
                                jarOut.putNextEntry(entry)
                                originalJar.getInputStream(entry).use { input ->
                                    input.copyTo(jarOut)
                                }
                                jarOut.closeEntry()
                                addedEntries.add(entry.name)
                            }
                        }
                }

                // Add Kotlin standard library
                val kotlinStdlib = File(CompilerConstants.getStdLib(settings))
                if (kotlinStdlib.exists()) {
                    addJarToFatJar(kotlinStdlib, jarOut, addedEntries)
                }

                // Add all resolved dependencies
                val resolvedDeps = dependencyService.getCachedDependencies()
                resolvedDeps
                    .map { File(it) }
                    .filter { it.exists() && it.extension == "jar" }
                    .forEach { depJar ->
                        addJarToFatJar(depJar, jarOut, addedEntries)
                    }
            }
        }

        Files.move(tmp, jarFile, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun addJarToFatJar(jarFile: File, jarOut: JarOutputStream, addedEntries: MutableSet<String>) {
        try {
            JarFile(jarFile).use { jar ->
                jar.entries().asSequence()
                    .filter { entry ->
                        !entry.name.startsWith("META-INF/") &&
                                entry.name != JarFile.MANIFEST_NAME &&
                                !addedEntries.contains(entry.name)
                    }
                    .forEach { entry ->
                        try {
                            jarOut.putNextEntry(java.util.jar.JarEntry(entry.name))
                            jar.getInputStream(entry).use { input ->
                                input.copyTo(jarOut)
                            }
                            jarOut.closeEntry()
                            addedEntries.add(entry.name)
                        } catch (e: Exception) {
                            // Skip duplicate entries or other issues
                            logger.println("Warning: Could not add entry ${entry.name} from ${jarFile.name}: ${e.message}")
                        }
                    }
            }
        } catch (e: Exception) {
            logger.println("Warning: Could not process JAR ${jarFile.name}: ${e.message}")
        }
    }

    private fun findKotlinFiles(): List<String> {
        val rootPath = projectRoot.toPath().toAbsolutePath().normalize()
        Files.walk(rootPath).use { stream ->
            return stream
                .filter { path ->
                    path.toString().endsWith(".kt") &&
                            !path.startsWith(rootPath.resolve(CompilerConstants.buildFolder)) &&
                            !path.toString().contains("gradle/wrapper") &&
                            !path.fileName.toString().endsWith(".gradle.kts") &&
                            path.fileName.toString() != "settings.gradle.kts"
                }
                .map { it.toAbsolutePath().toString() }
                .toList()
        }
    }
}
