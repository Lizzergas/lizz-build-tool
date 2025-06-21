package com.skommy.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.prompt
import com.skommy.getCurrentFolderName
import com.skommy.yaml.YamlWriter
import com.skommy.yaml.buildSettings

private const val DEFAULT_PROJECT_VERSION = "0.0.1"
private const val DEFAULT_KOTLIN_VERSION = "2.1.20"
private const val DEFAULT_MAIN_KT = "MainKt"

class Init : CliktCommand() {
    override fun run() {
        if (YamlWriter.exists()) {
            echo("lizz.yaml already exists", err = true)
            currentContext.exitProcess(1)
        }

        val projectName = terminal.prompt(
            prompt = "Project name:",
            default = getCurrentFolderName(),
            showDefault = true
        )
        val projectVersion = terminal.prompt(
            prompt = "Project version:",
            default = DEFAULT_PROJECT_VERSION,
            showDefault = true
        )

        val settings = buildSettings(
            projectName = projectName.orEmpty(),
            projectVersion = projectVersion.orEmpty(),
            DEFAULT_MAIN_KT,
            DEFAULT_KOTLIN_VERSION,
            listOf("guava", "gson", "okio")
        )
        YamlWriter.save(settings)
    }
}