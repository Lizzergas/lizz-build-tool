package com.skommy.yaml

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
    val mainClass: String,
)

@Serializable
data class KotlinSettings(
    val version: String,
)

fun buildSettings(
    projectName: String,
    projectVersion: String,
    mainClass: String,
    kotlinVersion: String,
    dependencies: List<String>
): BuildSettings {
    return BuildSettings(
        project = ProjectSettings(projectName, projectVersion, mainClass),
        kotlin = KotlinSettings(kotlinVersion),
        dependencies = dependencies
    )
}