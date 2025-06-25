package com.skommy.models

import com.skommy.BuildConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BuildSettings(
    @SerialName("project") val project: ProjectSettings,
    @SerialName("kotlin") val kotlin: KotlinSettings,
    @SerialName("dependencies") val dependencies: List<String>,
//    @SerialName("scripts") val scripts: Map<String, Script>,
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

@Serializable
sealed class Script {
    abstract val command: String

    @Serializable
    data class SimpleScript(override val command: String) : Script()

    @Serializable
    data class DetailedScript(
        @SerialName("src") override val command: String,
        @SerialName("desc") val description: String
    ) : Script()
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
//        scripts = scripts,
    )
}

// Convenience functions for creating scripts
fun simpleScript(command: String): Script = Script.SimpleScript(command)
fun detailedScript(command: String, description: String): Script = Script.DetailedScript(command, description)