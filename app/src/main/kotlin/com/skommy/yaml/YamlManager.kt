package com.skommy.yaml

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import kotlinx.serialization.encodeToString
import java.io.File
import java.nio.file.Files

private const val DEFAULT_YAML_NAME = "lizz.yaml"

class YamlManager {
    companion object {

        fun save(settings: BuildSettings, file: File = File(DEFAULT_YAML_NAME)) {
            val yamlText = Yaml.default.encodeToString(settings)
            file.writeText(yamlText)
            println("Yaml saved to ${file.absolutePath}")
        }

        fun load(): BuildSettings = load(File(DEFAULT_YAML_NAME))

        fun load(file: File): BuildSettings =
            Yaml.default.decodeFromStream(Files.newInputStream(file.toPath()))

        fun exists(dir: File = File(".")): Boolean =
            File(dir, DEFAULT_YAML_NAME).exists()
    }
}