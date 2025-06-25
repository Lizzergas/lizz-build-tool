package com.skommy.cli

import com.github.ajalt.clikt.core.Context
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * Clean command - removes the build directory and all compiled artifacts.
 * Uses LizzCommand base class for automatic project root detection.
 */
class Clean : LizzCommand() {
    override fun help(context: Context): String = "Remove build/ folder"

    @OptIn(ExperimentalPathApi::class)
    override fun runCommand() {
        val buildPath = buildDir().toPath()
        if (buildPath.toFile().exists()) {
            buildPath.deleteRecursively()
            echo("✓ Deleted build/ folder")
        } else {
            echo("Build folder does not exist - nothing to clean")
        }
    }
}
