package com.skommy.cli

import com.skommy.services.DependencyService
import com.skommy.services.YamlService

/**
 * Sync command - resolves and caches all project dependencies.
 * Uses LizzCommand base class for automatic project root detection.
 * This is now a pure controller that delegates to DependencyService.
 */
class Sync : LizzCommand() {
    
    override fun runCommand() {
        val dependencyService = DependencyService(root)
        val settings = YamlService.loadFromFile(lizzYamlFile())

        if (settings.dependencies.isEmpty()) {
            echo("No dependencies to sync")
            return
        }

        echo("Syncing dependencies...")
        
        val resolvedDependencies = dependencyService.resolveAndCacheDependencies(settings)
        
        if (resolvedDependencies.isNotEmpty()) {
            echo("✓ Sync completed. ${resolvedDependencies.size} dependencies resolved.")
            echo("✓ Dependencies saved to: ${dependencyService.getCacheFile().absolutePath}")
            
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
