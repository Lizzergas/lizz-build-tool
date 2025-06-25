package com.skommy.cli

import com.skommy.BuildConstants
import com.skommy.compiler.LizzJVMCompiler
import com.skommy.services.DependencyService
import com.skommy.services.YamlService
import org.jetbrains.kotlin.cli.common.ExitCode

/**
 * Build command - compiles Kotlin code and creates a runnable JAR.
 * Uses LizzCommand base class for automatic project root detection.
 * Uses DependencyService for clean separation of concerns.
 */
class Build : LizzCommand() {

    override fun runCommand() {
        val dependencyService = DependencyService(root)
        val mainKt = rootFile(BuildConstants.MAIN_KT)
        val settings = YamlService.loadFromFile(lizzYamlFile())

        echo("Building ${settings.project.name} v${settings.project.version}...")

        // Ensure dependencies are resolved (will sync if needed)
        if (dependencyService.needsSync(settings)) {
            echo("Dependencies out of sync. Running sync first...")
            dependencyService.resolveAndCacheDependencies(settings)
        }

        val compiler = LizzJVMCompiler(settings, root)

        if (mainKt.exists()) {
            val exit = compiler.compileKotlin()
            if (exit == ExitCode.OK) {
                compiler.updateJarManifest()
                echo("✓ Build completed successfully!")
                echo("✓ Runnable JAR created at: ${compiler.jarPath}")
                if (settings.dependencies.isNotEmpty()) {
                    echo("✓ All dependencies included in JAR")
                }
            } else {
                echo("✗ Build failed with exit code: ${exit.name}", err = true)
            }
        } else {
            echo(
                "✗ ${BuildConstants.MAIN_KT} was not found in project root directory: ${root.absolutePath}",
                err = true
            )
        }
    }
}
