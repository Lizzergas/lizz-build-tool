package com.skommy.cli

import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * Clean command - removes the build directory and all compiled artifacts.
 * Uses LizzCommand base class for automatic project root detection.
 */
class Clean : LizzCommand() {
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
