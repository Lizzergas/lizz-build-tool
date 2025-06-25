package com.skommy.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.skommy.BuildConstants
import java.io.File

/**
 * Base class for all Lizz commands that require a project root.
 * Automatically finds and validates the project root directory containing lizz.yaml.
 * Provides a non-nullable `root` property that child commands can use safely.
 */
abstract class LizzCommand : CliktCommand() {

    /**
     * The project root directory containing lizz.yaml.
     * This property is guaranteed to be non-null when runCommand() is called.
     */
    protected lateinit var root: File
        private set

    /**
     * Finds the project root by walking up the directory tree from the current working directory
     * until it finds a directory containing lizz.yaml.
     *
     * @return The project root directory, or null if not found
     */
    private fun findProjectRoot(): File? {
        return generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
            .firstOrNull { File(it, BuildConstants.CONFIG_FILE).exists() }
    }

    /**
     * Validates that we're in a Lizz project and initializes the root property.
     * This method is called automatically before runCommand().
     */
    private fun validateProjectRoot() {
        val projectRoot = findProjectRoot()

        if (projectRoot == null) {
            echo("✗ ${BuildConstants.CONFIG_FILE} not found in this directory or any parent directories.", err = true)
            echo("Please ensure you're inside a Lizz project directory.", err = true)
            throw com.github.ajalt.clikt.core.Abort()
        }

        root = projectRoot
        // Only show project root if it's different from current directory
        if (root.absolutePath != File(System.getProperty("user.dir")).absolutePath) {
            echo("Using project root: ${root.absolutePath}")
        }
    }

    /**
     * Final run method that handles project root validation.
     * Child classes should override runCommand() instead of run().
     */
    final override fun run() {
        validateProjectRoot()
        runCommand()
    }

    /**
     * Abstract method that child commands must implement.
     * When this is called, the root property is guaranteed to be initialized.
     */
    protected abstract fun runCommand()

    /**
     * Helper method to get a file relative to the project root.
     * @param relativePath The path relative to the project root
     * @return File object pointing to the specified path
     */
    protected fun rootFile(relativePath: String): File {
        return File(root, relativePath)
    }

    /**
     * Helper method to get the lizz.yaml file in the project root.
     * @return File object pointing to lizz.yaml
     */
    protected fun yamlFile(): File {
        return rootFile(BuildConstants.CONFIG_FILE)
    }

    /**
     * Helper method to get the build directory in the project root.
     * @return File object pointing to the build directory
     */
    protected fun buildDir(): File {
        return rootFile("build")
    }
}
