package com.skommy.cli

import com.github.ajalt.clikt.core.Context
import com.skommy.CompilerConstants
import com.skommy.gradle.GradleBuild
import com.skommy.services.BuildService
import com.skommy.services.DependencyService
import java.io.File

/**
 * Sync command - resolves and caches all project dependencies.
 * Uses LizzCommand base class for automatic project root detection.
 * This is now a pure controller that delegates to DependencyService.
 */
class Sync : LizzCommand() {
    override fun help(context: Context): String = "Resolve dependencies if they weren't resolved yet"
    override fun runCommand() {
        val buildService = BuildService()
        val dependencyResolveService = DependencyService(projectRoot = root)
        val gradleBuild = GradleBuild()
        val settings = buildService.load(yamlFile())

        if(buildService.yamlExists().not()) {
            echo("Project is not defined")
            return
        }

        // Ensure Gradle files exist before syncing
        val buildFile = File("build.gradle.kts")
        echo("Checking for build.gradle.kts at: ${buildFile.absolutePath}")
        if (!buildFile.exists()) {
            echo("Creating Gradle files...")
            gradleBuild.stubGradleSetup(CompilerConstants.getKotlinHome(settings))
            echo("Gradle files created")
        } else {
            echo("build.gradle.kts already exists")
        }

        if (settings.dependencies.isEmpty()) {
            echo("No dependencies to sync")
            // Still update Gradle even if no dependencies to sync reflection setting
            gradleBuild.syncGradleStub(emptyList(), settings)
            echo("✓ Gradle build file updated")
            return
        }

        echo("Syncing dependencies...")

        val resolvedDependencies = dependencyResolveService.resolveAndCacheDependencies(settings)

        if (resolvedDependencies.isNotEmpty()) {
            echo("✓ Sync completed. ${resolvedDependencies.size} dependencies resolved.")
            echo("✓ Dependencies saved to: ${dependencyResolveService.getCacheFile().absolutePath}")

            // Show resolved dependencies
            echo("\nResolved dependencies:")
            resolvedDependencies.forEach { dep ->
                echo("  - ${java.io.File(dep).name}")
            }

            // Update Gradle build file with resolved dependencies and reflection setting
            val resolvedJarFiles = resolvedDependencies.map { File(it) }
            gradleBuild.syncGradleStub(resolvedJarFiles, settings)
            echo("✓ Gradle build file updated")
        } else {
            echo("✗ No dependencies were resolved")
            // Still update Gradle with reflection setting even if no dependencies resolved
            gradleBuild.syncGradleStub(emptyList(), settings)
            echo("✓ Gradle build file updated")
        }
    }
}
