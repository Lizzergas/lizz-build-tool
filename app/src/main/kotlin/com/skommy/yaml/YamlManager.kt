package com.skommy.yaml

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import kotlinx.serialization.encodeToString
import java.io.File
import java.nio.file.Files

private const val DEFAULT_YAML_NAME = "lizz.yaml"

class YamlManager {
    companion object {
        fun save(settings: BuildSettings) {
            val file = File(DEFAULT_YAML_NAME)
            val result = Yaml.default.encodeToString(settings)
            file.writeText(result)
            println("Yaml saved to lizz.yaml")
        }

        fun load(): BuildSettings {
            val file = File(DEFAULT_YAML_NAME)
            val result = Yaml.default.decodeFromStream<BuildSettings>(stream = Files.newInputStream(file.toPath()))
            return result
        }

        fun exists(): Boolean {
            val file = File(DEFAULT_YAML_NAME)
            return file.exists()
        }
    }
}