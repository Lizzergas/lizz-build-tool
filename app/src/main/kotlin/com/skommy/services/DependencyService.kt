package com.skommy.services

import com.skommy.CompilerConstants
import com.skommy.models.BuildSettings
import com.skommy.resolver.MavenResolver
import com.skommy.services.Logger
import java.io.File

/**
 * Service responsible for managing project dependencies.
 * Handles resolution, caching, and providing dependency information.
 */
class DependencyService(
    private val projectRoot: File = File(System.getProperty("user.dir"))
) {
    private val dependenciesFile = File(projectRoot, "${CompilerConstants.buildFolder}/dependencies.txt")
    private val mavenResolver = MavenResolver(projectRoot = projectRoot)

    /**
     * Resolves all dependencies from build settings and saves them to cache file.
     * @param settings The build settings containing dependency coordinates
     * @return List of resolved JAR file paths
     */
    fun resolveAndCacheDependencies(settings: BuildSettings): List<String> {
        Logger.println("Resolving dependencies for ${settings.project.name}")
        if (settings.dependencies.isEmpty()) {
            return emptyList()
        }

        // Ensure build directory exists
        val buildDir = File(projectRoot, CompilerConstants.buildFolder)
        if (!buildDir.exists()) {
            buildDir.mkdirs()
        }

        val allClasspaths = mavenResolver.resolveAll(settings.dependencies)

        // Save all resolved dependencies to cache file
        val classpath = allClasspaths.joinToString(File.pathSeparator)
        dependenciesFile.writeText(classpath)

        return allClasspaths
    }

    /**
     * Gets cached dependencies from the dependencies file.
     * @return List of cached dependency paths, or empty list if no cache exists
     */
    fun getCachedDependencies(): List<String> {
        return if (dependenciesFile.exists()) {
            val content = dependenciesFile.readText().trim()
            if (content.isBlank()) {
                emptyList()
            } else {
                content.split(File.pathSeparator).filter { it.isNotBlank() }
            }
        } else {
            emptyList()
        }
    }

    /**
     * Checks if dependencies need to be resolved/synced.
     * @param settings The build settings to check
     * @return true if sync is needed, false otherwise
     */
    fun needsSync(settings: BuildSettings): Boolean {
        if (settings.dependencies.isEmpty()) {
            return false
        }

        if (!dependenciesFile.exists()) {
            return true
        }

        // Could add more sophisticated checks here like:
        // - Check if lizz.yaml is newer than dependencies.txt
        // - Check if dependencies list has changed
        // - Check if any dependency coordinates have changed

        return false
    }

    /**
     * Gets all dependencies required for compilation (cached + Kotlin stdlib + optional reflection).
     * @param settings Build settings to determine Kotlin home path and reflection requirement
     * @return List of all JAR paths needed for compilation
     */
    fun getCompilationClasspath(settings: BuildSettings): List<String> {
        val classpaths = mutableListOf<String>()

        // Add Kotlin standard library
        classpaths.add(CompilerConstants.getStdLib(settings))

        // Add Kotlin reflection library if enabled
        if (settings.kotlin.reflection) {
            classpaths.add(CompilerConstants.getReflect(settings))
        }

        // Add cached dependencies
        classpaths.addAll(getCachedDependencies())

        return classpaths
    }

    /**
     * Gets the path to the dependencies cache file.
     */
    fun getCacheFile(): File = dependenciesFile
}
