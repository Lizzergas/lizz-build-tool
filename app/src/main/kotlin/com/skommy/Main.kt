/*
 * This source file was generated by the Gradle 'init' task
 */
package com.skommy

import BuildConfig
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import com.skommy.cli.*
import com.skommy.services.Logger

class Lizz : CliktCommand(name = "lizz") {
    init {
        versionOption(version = BuildConfig.VERSION)
    }

    override fun run() {
        Logger.initialize(currentContext)
    }
}

fun main(args: Array<String>) {
    Lizz()
        .subcommands(Init(), Build(), Run(), Clean(), Sync(), Sh())
        .main(args)
}
