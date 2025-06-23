package com.skommy.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.skommy.CompilerConstants
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

class Clean : CliktCommand() {
    @OptIn(ExperimentalPathApi::class)
    override fun run() {
        val buildPath = Path.of(CompilerConstants.buildFolder)
        buildPath.deleteRecursively()
        println("Deleted build/ folder")
    }
}