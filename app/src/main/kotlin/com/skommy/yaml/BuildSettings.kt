package com.skommy.yaml

import com.skommy.BuildConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BuildSettings(
    @SerialName("project") val project: ProjectSettings,
    @SerialName("kotlin") val kotlin: KotlinSettings,
    @SerialName("dependencies") val dependencies: List<String>,
)

@Serializable
data class ProjectSettings(
    val name: String,
    val version: String,
    val author: String,
    val description: String = "",
    val mainClass: String = BuildConstants.MAIN_KT,
)

@Serializable
data class KotlinSettings(
    val version: String,
)

fun buildSettings(
    name: String,
    version: String,
    description: String = "",
    author: String,
    mainClass: String = BuildConstants.MAIN_KT,
    kotlinVersion: String = BuildConstants.KOTLIN_VERSION,
    dependencies: List<String>
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
        dependencies = dependencies
    )
}