package com.skommy.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.skommy.compiler.LizzJVMCompiler
import com.skommy.yaml.YamlManager
import org.jetbrains.kotlin.cli.common.ExitCode
import java.io.File

class Build : CliktCommand() {
    override fun run() {
        val mainKt = File("main.kt")
        val settings = YamlManager.load()
        val compiler = LizzJVMCompiler(settings)

        if (mainKt.exists()) {
            val exit = compiler.compileKotlin()
            if (exit == ExitCode.OK) {
                compiler.updateJarManifest()
            }
        } else {
            echo("main.kt was not found", err = true)
        }
    }
}