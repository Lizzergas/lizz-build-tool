package com.skommy.cli

import com.github.ajalt.clikt.core.Context
import com.skommy.services.BuildService
import com.skommy.services.DependencyService

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
        val settings = buildService.load(yamlFile())

        if (settings.dependencies.isEmpty()) {
            echo("No dependencies to sync")
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
        } else {
            echo("✗ No dependencies were resolved")
        }
    }
}
