package com.skommy.compiler

import com.skommy.CompilerConstants
import com.skommy.models.BuildSettings
import com.skommy.services.DependencyService
import com.skommy.services.Logger
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
    private val projectRoot: File = File(System.getProperty("user.dir"))
) {
    val jarPath = File(projectRoot, "${CompilerConstants.buildFolder}/${settings.project.name}.jar").absolutePath
    private val dependencyService = DependencyService(projectRoot = projectRoot)

    private fun buildClasspath(): String {
        return dependencyService.getCompilationClasspath(settings).joinToString(File.pathSeparator)
    }

    fun compileKotlin(): ExitCode {
        Logger.println("Compiling Kotlin files...")

        val classpath = buildClasspath()
        Logger.println("Using classpath: $classpath")

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
        Logger.println("Exit: ${exit.name} ${exit.code}")
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
        Logger.println("classpath: $manifestClasspath")
        Logger.println("Created fat JAR with all dependencies")
    }

    private fun buildManifestClasspath(): String {
        val classpaths = mutableListOf<String>()

        // Add Kotlin standard library (use relative path or include in fat JAR)
        val kotlinStdlib = File(CompilerConstants.getStdLib(settings))
        if (kotlinStdlib.exists()) {
            classpaths.add(kotlinStdlib.name)
        } else {
            Logger.println("KOTLIN_HOME was not set properly for path: ${CompilerConstants.getStdLib(settings)}")
        }

        // Add Kotlin reflection library if enabled
        if (settings.kotlin.reflection) {
            val kotlinReflect = File(CompilerConstants.getReflect(settings))
            if (kotlinReflect.exists()) {
                classpaths.add(kotlinReflect.name)
            } else {
                Logger.println("KOTLIN_HOME was not set properly for reflection path: ${CompilerConstants.getReflect(settings)}")
            }
        }

        // For fat JAR, we don't need external classpath entries since everything is included
        // But we keep this for compatibility if someone wants to use external JARs
        val resolvedDeps = dependencyService.getCachedDependencies()
        val resolvedDepNames = resolvedDeps.map { File(it).name }

        // Deduplicate dependencies to avoid conflicts (e.g., kotlin-stdlib from KOTLIN_HOME vs transitive)
        val deduplicatedDeps = deduplicateJarNames(classpaths + resolvedDepNames)

        return deduplicatedDeps.joinToString(" ")
    }

    /**
     * Deduplicates JAR names by removing duplicates based on artifact base names.
     * Prioritizes JARs that appear earlier in the list (e.g., KOTLIN_HOME over transitive dependencies).
     * 
     * @param jarNames List of JAR file names to deduplicate
     * @return Deduplicated list of JAR names
     */
    private fun deduplicateJarNames(jarNames: List<String>): List<String> {
        val seenArtifacts = mutableSetOf<String>()
        val result = mutableListOf<String>()

        for (jarName in jarNames) {
            val artifactBaseName = extractArtifactBaseName(jarName)

            if (!seenArtifacts.contains(artifactBaseName)) {
                seenArtifacts.add(artifactBaseName)
                result.add(jarName)
            } else {
                Logger.println("Skipping duplicate dependency: $jarName (already have $artifactBaseName)")
            }
        }

        return result
    }

    /**
     * Extracts the base artifact name from a JAR file name.
     * Examples:
     * - "kotlin-stdlib.jar" -> "kotlin-stdlib"
     * - "kotlin-stdlib-2.1.21.jar" -> "kotlin-stdlib"
     * - "gson-2.10.1.jar" -> "gson"
     * 
     * @param jarName The JAR file name
     * @return The base artifact name without version and extension
     */
    private fun extractArtifactBaseName(jarName: String): String {
        // Remove .jar extension
        val nameWithoutExtension = jarName.removeSuffix(".jar")

        // Find the last occurrence of a version pattern (number followed by dot or dash)
        // This handles cases like "kotlin-stdlib-2.1.21" -> "kotlin-stdlib"
        val versionPattern = Regex("-\\d+(\\.\\d+)*(-.*)?$")
        return versionPattern.replace(nameWithoutExtension, "")
    }

    /**
     * Deduplicates JAR files by removing duplicates based on artifact base names.
     * Prioritizes JARs that appear earlier in the list (e.g., KOTLIN_HOME over transitive dependencies).
     * 
     * @param jarFiles List of JAR files to deduplicate
     * @return Deduplicated list of JAR files
     */
    private fun deduplicateJarFiles(jarFiles: List<File>): List<File> {
        val seenArtifacts = mutableSetOf<String>()
        val result = mutableListOf<File>()

        for (jarFile in jarFiles) {
            val artifactBaseName = extractArtifactBaseName(jarFile.name)

            if (!seenArtifacts.contains(artifactBaseName)) {
                seenArtifacts.add(artifactBaseName)
                result.add(jarFile)
            } else {
                Logger.println("Skipping duplicate dependency JAR: ${jarFile.name} (already have $artifactBaseName)")
            }
        }

        return result
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

                // Collect all dependency JARs and deduplicate them
                val allDependencyJars = mutableListOf<File>()

                // Add Kotlin standard library
                val kotlinStdlib = File(CompilerConstants.getStdLib(settings))
                if (kotlinStdlib.exists()) {
                    allDependencyJars.add(kotlinStdlib)
                }

                if (settings.kotlin.reflection) {
                    val reflection = File(CompilerConstants.getReflect(settings))
                    if (reflection.exists()) {
                        allDependencyJars.add(reflection)
                    }
                }

                // Add all resolved dependencies
                val resolvedDeps = dependencyService.getCachedDependencies()
                resolvedDeps
                    .map { File(it) }
                    .filter { it.exists() && it.extension == "jar" }
                    .forEach { depJar ->
                        allDependencyJars.add(depJar)
                    }

                // Deduplicate JARs based on artifact base names (prioritizes earlier entries)
                val deduplicatedJars = deduplicateJarFiles(allDependencyJars)

                // Add deduplicated JARs to fat JAR
                deduplicatedJars.forEach { depJar ->
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
                            Logger.println("Warning: Could not add entry ${entry.name} from ${jarFile.name}: ${e.message}")
                        }
                    }
            }
        } catch (e: Exception) {
            Logger.println("Warning: Could not process JAR ${jarFile.name}: ${e.message}")
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
