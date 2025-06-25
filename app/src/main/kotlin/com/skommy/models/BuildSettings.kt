package com.skommy.models

import com.skommy.BuildConstants
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class BuildSettings(
    @SerialName("project") val project: ProjectSettings,
    @SerialName("kotlin") val kotlin: KotlinSettings,
    @SerialName("dependencies") val dependencies: List<String>,
    @SerialName("scripts") val scripts: Map<String, Script>,
)

@Serializable
data class ProjectSettings(
    val name: String,
    val version: String,
    val author: String,
    val description: String = "",
    val mainClass: String = BuildConstants.MAIN_KT_CLASS,
)

@Serializable
data class KotlinSettings(
    val version: String,
)

@Serializable(with = ScriptSerializer::class)
sealed class Script(
    open val command: String
) {
    data class SimpleScript(
        override val command: String
    ) : Script(command)
}

object ScriptSerializer : KSerializer<Script> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Script", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Script) {
        encoder.encodeString(value.command)
    }

    override fun deserialize(decoder: Decoder): Script {
        val command = decoder.decodeString()
        return Script.SimpleScript(command)
    }
}


fun buildSettings(
    name: String,
    version: String,
    description: String = "",
    author: String,
    mainClass: String = BuildConstants.MAIN_KT_CLASS,
    kotlinVersion: String = BuildConstants.KOTLIN_VERSION,
    dependencies: List<String>,
    scripts: Map<String, Script> = emptyMap(),
): BuildSettings {
    return BuildSettings(
        project = ProjectSettings(
            name = name,
            version = version,
            author = author,
            description = description,
            mainClass = mainClass
        ),
        kotlin = KotlinSettings(version = kotlinVersion),
        dependencies = dependencies,
        scripts = scripts,
    )
}

// Convenience functions for creating scripts
fun simpleScript(command: String): Script = Script.SimpleScript(command)