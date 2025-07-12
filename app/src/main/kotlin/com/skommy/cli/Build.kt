package com.skommy.cli

import com.github.ajalt.clikt.core.Context
import com.skommy.BuildConstants
import com.skommy.compiler.LizzJVMCompiler
import com.skommy.services.BuildService
import com.skommy.services.DependencyService
import org.jetbrains.kotlin.cli.common.ExitCode

/**
 * Build command - compiles Kotlin code and creates a runnable JAR.
 * Uses LizzCommand base class for automatic project root detection.
 * Uses DependencyResolverService for clean separation of concerns.
 */
class Build : LizzCommand() {

    override fun help(context: Context): String = "Build Kotlin using K2JVMCompiler and output in builds folder"
    override fun runCommand() {
        val buildService = BuildService()
        val dependencyResolveService = DependencyService(projectRoot = root)
        val mainKt = rootFile(BuildConstants.MAIN_KT)
        val settings = buildService.load(yamlFile())

        echo("Building ${settings.project.name} v${settings.project.version}...")

        // Ensure dependencies are resolved (will sync if needed)
        if (dependencyResolveService.needsSync(settings)) {
            echo("Dependencies out of sync. Running sync first...")
            dependencyResolveService.resolveAndCacheDependencies(settings)
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
