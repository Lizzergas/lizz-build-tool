package com.skommy.yaml

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.encodeToString
import java.io.File

private const val DEFAULT_YAML_NAME = "lizz.yaml"

class YamlWriter {
    companion object {
        fun save(settings: BuildSettings) {
            val file = File(DEFAULT_YAML_NAME)
            val result = Yaml.default.encodeToString(settings)
            file.writeText(result)
            println("Yaml saved to lizz.yaml")
        }

        fun exists(): Boolean {
            val file = File(DEFAULT_YAML_NAME)
            return file.exists()
        }
    }
}