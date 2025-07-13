package com.skommy.services

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.skommy.BuildConstants
import com.skommy.models.BuildSettings
import kotlinx.serialization.encodeToString
import java.io.File
import java.nio.file.Files

class BuildService() {
    /**
     * Saves BuildSettings to lizz.yaml in the specified project root directory.
     * @param settings The BuildSettings to save
     * @param projectRoot The project root directory (defaults to current directory for backward compatibility)
     */
    fun save(settings: BuildSettings, projectRoot: File = File(".")) {
        val file = File(projectRoot, BuildConstants.CONFIG_FILE)
        val yamlText = Yaml.Companion.default.encodeToString(settings)
        file.writeText(yamlText)
    }

    /**
     * Loads BuildSettings from the specified file.
     * @param file The file to load from
     * @return The loaded BuildSettings
     */
    fun load(file: File): BuildSettings =
        Yaml.Companion.default.decodeFromStream(Files.newInputStream(file.toPath()))

    /**
     * Checks if lizz.yaml exists in the specified directory.
     * @param dir The directory to check (defaults to current directory)
     * @return true if lizz.yaml exists in the directory
     */
    fun yamlExists(dir: File = File(".")): Boolean =
        File(dir, BuildConstants.CONFIG_FILE).exists()
}
